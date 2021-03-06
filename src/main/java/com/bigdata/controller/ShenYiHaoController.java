package com.bigdata.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bigdata.model.PaginatedResult;
import com.bigdata.model.RiskControlResult;
import com.bigdata.model.RiskControlRule;
import com.bigdata.model.vo.RiskControlReport;
import com.bigdata.service.RiskControlDataService;
import com.bigdata.service.RiskControlService;
import com.bigdata.service.SBBlankResultService;
import com.bigdata.service.SBOrderService;
import com.bigdata.util.DateUtils;
import com.bigdata.util.Enum.PageConstant;
import com.bigdata.util.Enum.ResponseResultEnum;
import com.bigdata.util.HttpConnectionUtils;
import com.bigdata.util.PageUtil;
import com.bigdata.util.ResultBody;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Desciption 深一号决策流调用
 * Create By  li.bo
 * CreateTime 2018/01/26 18:14
 * UpdateTime 2018/01/26 18:14
 */
@RestController
@RequestMapping("/syh")
@Api(value = "syh", description = "深一号风控", produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
public class ShenYiHaoController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${flow.shenyihao}")
    private String api_url;

    @Autowired
    private SBOrderService sbOrderService;

    @Autowired
    private RiskControlDataService riskControlDataService;

    @Autowired
    private RiskControlService riskControlService;

    @Autowired
    private SBBlankResultService sbBlankResultService;

    @RequestMapping(value = "", method = RequestMethod.POST)
    @ApiOperation(value = "调用风控接口", notes = "调用风控系统，结果入库")
    public ResultBody<? extends Object> getDetail(HttpServletRequest request, HttpServletResponse response) {

        /* 入参 **/
        JSONObject obj = riskControlDataService.init();

        // 转发请求到服务端
        String result = HttpConnectionUtils.setConnection(this.api_url, obj);
        JSONObject resultObj = JSONObject.parseObject(result);
        logger.info("返回结果： " + resultObj.toString());

        try {
            // 触发规则
            JSONArray ruleArray = Optional.ofNullable(resultObj)
                    .map(a -> a.getBoolean("success") ? a.getJSONObject("data").getJSONArray("nodeResults") : null)
                    .get();
            List<JSONObject> rules = new ArrayList<>();
            // 正则判断：字符串是否有数字（普通规则）
            Pattern p = Pattern.compile("[0-9]");
            ruleArray.forEach(a -> {
                JSONArray risks = JSON.parseObject(a.toString()).getJSONObject("result").getJSONArray("risks");
                String taskName = JSON.parseObject(a.toString()).getString("taskName");
                risks.forEach(risk -> {
                    JSONObject r = JSON.parseObject(risk.toString());
                    String tmp = r.getString("ruleName");
                    JSONObject rule = new JSONObject();

                    String ruleCode = tmp.substring(tmp.indexOf(":") + 1, tmp.lastIndexOf(":"));
                    Matcher m = p.matcher(ruleCode);
                    if (m.find()) {
                        rule.put("taskName", taskName);
                        rule.put("ruleCode", ruleCode);
                        rule.put("ruleName", tmp.substring(tmp.lastIndexOf(":") + 1, tmp.length()));
                        rules.add(rule);

                    }

                });
            });
            logger.info("rules:{}", rules);

            // 风控结果
            JSONObject res = Optional.ofNullable(resultObj)
                    .map(a -> a.getJSONObject("data").getJSONArray("nodeResults"))
                    .map(a -> a.get(a.size() - 1).toString())
                    .map(a -> JSON.parseObject(a).getJSONObject("result").getJSONArray("items"))
                    .map(a -> a.get(a.size() - 1).toString())
                    .map(JSON::parseObject)
                    .get();

            // 风控计算数据入库
            RiskControlResult riskControlResult = new RiskControlResult();
            riskControlResult.setResult(res.toJSONString());
            riskControlResult.setRule(rules.toString());
            riskControlResult.setYear(DateUtils.getYear(-1));
            riskControlResult.setMonth(DateUtils.getMonth(-1));
            riskControlService.saveOneResult(riskControlResult);

            return new ResultBody(ResponseResultEnum.SUCCESS.getFeatureType(), ResponseResultEnum.SUCCESS.getDescription(), res);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultBody(ResponseResultEnum.RETRY.getFeatureType(), ResponseResultEnum.RETRY.getDescription(), "");
        }

//        return new ResultBody(ResponseResultEnum.SUCCESS.getFeatureType(), ResponseResultEnum.SUCCESS.getDescription(), resultObj.getJSONObject("data"));
    }

    @PostMapping("/order")
    public ResultBody<? extends Object> getOrders(HttpServletRequest request, @RequestBody String paramsString, HttpServletResponse response) {

        JSONObject params = JSON.parseObject(paramsString);
        log.info("params:{}", params);

        int page = PageUtil.parsePage(params.getString("pageString"), PageConstant.PAGE);
        int perPage = PageUtil.parsePerPage(params.getString("perPageString"), PageConstant.PER_PAGE);

        Map<String, Object> map = Maps.newHashMap();
        try {
            map.put("startDate", Optional
                    .ofNullable(params.getJSONArray("rq"))
                    .map(a -> a.size() > 0 ? a.get(0) : "")
                    .get());

            map.put("endDate", Optional
                    .ofNullable(params.getJSONArray("rq"))
                    .map(a -> a.size() > 1 ? a.get(1) : "")
                    .get());
        } catch (Exception e) {
            e.printStackTrace();
        }
        map.put("ghdw", params.getString("ghdw").replaceAll("\\s*", "")); // 去掉空格
        map.put("page", page);
        map.put("perPage", perPage);

        log.info("map:{}", map);

        PaginatedResult result = new PaginatedResult()
                .setData(sbOrderService.listOrders(map))
                .setCurrentPage(page)
                .setTotal(sbOrderService.countOrders(map))
                .setTotalPage(PageUtil.calculateTotalPage(sbOrderService.countOrders(map), perPage));

        return new ResultBody(ResponseResultEnum.SUCCESS.getFeatureType(), ResponseResultEnum.SUCCESS.getDescription(), result);
    }

    @PostMapping("/blankResult")
    public ResultBody<? extends Object> getBlankResult(HttpServletRequest request, HttpServletResponse response) {

        sbBlankResultService.save();
        return new ResultBody(ResponseResultEnum.SUCCESS.getFeatureType(), ResponseResultEnum.SUCCESS.getDescription(), "");
    }

    @PostMapping("/riskControl")
    public ResultBody<? extends Object> getRiskControl(HttpServletRequest request, HttpServletResponse response) {

        List<RiskControlRule> rules = riskControlService.getAllRules();
        RiskControlResult riskControlResult = riskControlService.getLastestResult();
        JSONObject riskResult = JSON.parseObject(riskControlResult.getResult());
        String ruleResult = riskControlResult.getRule();
        // todo
        List<RiskControlReport> res = Lists.newArrayList();
        rules.forEach(rule -> {
            RiskControlReport riskControlReport = new RiskControlReport();
            riskControlReport.setType(rule.getType());
            riskControlReport.setSubType(rule.getType() + (rule.getSubType() == null ? "" : rule.getSubType()));
            riskControlReport.setMonitorContent(rule.getMonitorContent());
            riskControlReport.setThreshold(rule.getThreshold());
            String unit = rule.getUnit() == null ? "" : rule.getUnit();

            String tmp = rule.getValue();
            // 计算结果
            StringBuffer value = new StringBuffer();
            if (StringUtils.containsAny(tmp, ",")) {
                String[] tmps = tmp.split(",");
                for (String s : tmps) {
                    value.append(riskResult.get(s)).append(unit).append(" | ");
                }
                value.delete(value.length() - 3, value.length() - 1);
            } else {
                if ("是否".equals(unit)) {
                    value.append(riskResult.get(tmp).toString().equals("0") ? "否" : "是");
                } else {
                    value.append(riskResult.get(tmp)).append(unit);
                }
            }
            riskControlReport.setValue(value.toString());
            // 计算是否触发
            riskControlReport.setTrigger(StringUtils.contains(ruleResult, rule.getRule()) ? 1 : 0);
            res.add(riskControlReport);
        });
        Map<String, List<RiskControlReport>> result = res.stream().collect(Collectors.groupingBy(RiskControlReport::getSubType));

        return new ResultBody(ResponseResultEnum.SUCCESS.getFeatureType(), ResponseResultEnum.SUCCESS.getDescription(), result);
    }

}
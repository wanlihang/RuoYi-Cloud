package com.ilab.service.algorithm;

import com.alibaba.fastjson.JSONObject;
import com.ilab.service.model.NodeInfo;
import com.ilab.service.model.TaskInfo;
import com.ilab.service.util.TaskCostCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

import static com.ilab.service.constant.NodeInfoConstant.NODE_NAME_LIST;

@Slf4j
public class NSGA3Algorithm {
    public static NodeInfo getNodeInfo(TaskInfo taskInfo) {
        NodeInfo result = new NodeInfo();

        List<Double> state = getStateInfo(taskInfo);
        Integer index = getNSGA3Action(state);
        result.setNodeName(NODE_NAME_LIST.get(index));
        log.info("[DERLAlgorithm] [getNodeInfo] [调度到节点: {}]", result.getNodeName());
        return result;
    }

    private static List<Double> getStateInfo(TaskInfo taskInfo) {
        List<Double> result = new ArrayList<>();
        // 1. 每个节点的负载数据
        List<Double> nodeLoadList = TaskCostCountUtil.getNodeLoadList();
        result.addAll(nodeLoadList);

        // 2. 任务在每个节点的代价
        List<Double> taskCostList = TaskCostCountUtil.getTaskCostList(taskInfo.getEnvId());
        result.addAll(taskCostList);

        // 3. 任务在每个节点需要的等待时间
        List<Double> waitTimeList = TaskCostCountUtil.getWaitTimeList(taskInfo.getEnvId());
        result.addAll(waitTimeList);

        return result;
    }

    private static Integer getNSGA3Action(List<Double> state) {
        String u = "http://192.168.103.66:5003/getNSGA3Action";
        List<Double> params = new ArrayList<>(state);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/json; charset=UTF-8"));
        headers.add("Accept", MediaType.APPLICATION_JSON.toString());
        org.springframework.http.HttpEntity<String> httpEntity = new HttpEntity<>(JSONObject.toJSONString(params), headers);
        RestTemplate rst = new RestTemplate();

        ResponseEntity<Integer> responseEntity = TaskCostCountUtil.postForEntity(rst, state, httpEntity, u);

        return responseEntity.getBody();
    }
}

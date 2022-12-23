package com.advancedweb2022groupylt.finalpj.service.logic.http;

import com.advancedweb2022groupylt.finalpj.bean.entity.User;
import com.advancedweb2022groupylt.finalpj.bean.http.response.BackstageInfoResp;
import com.advancedweb2022groupylt.finalpj.bean.http.response.Message;
import com.advancedweb2022groupylt.finalpj.repository.UserRepository;
import com.advancedweb2022groupylt.finalpj.service.jwtService.JWTService;
import com.advancedweb2022groupylt.finalpj.service.jwtService.JWTStatus;
import com.advancedweb2022groupylt.finalpj.service.logic.middleStage.SceneTimeRecordService;
import com.advancedweb2022groupylt.finalpj.service.logic.ws.wsMpService2.UserRoomTable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class BackStageServiceImpl implements BackStageService
{
    @Resource
    private JWTService jwtService;
    @Resource
    private UserRepository userRepository;
    @Resource
    private SceneTimeRecordService sceneTimeRecordService;

    @Override
    public BackstageInfoResp queryInfo(String username)
    {
        User user = userRepository.findUserByUsername(username);
        BackstageInfoResp resp = new BackstageInfoResp();
        resp.setMessage(new Message(true,"query success"));
        List<BackstageInfoResp.InfoItem> items = new ArrayList<>();
        items.add(new BackstageInfoResp.InfoItem("邮箱",user.getEmail()));
        items.add(new BackstageInfoResp.InfoItem("上次登录时间",user.getLastLoginTimeStr()));
        items.add(new BackstageInfoResp.InfoItem("本次登录时间",user.getCurrentLoginTimeStr()));
        items.add(new BackstageInfoResp.InfoItem("上次选择的场景",user.getScene().toString()));
        for (int i = 1; i < UserRoomTable.ROOM_CNT; i++)
        {
            int seconds = sceneTimeRecordService.get(username,i);
            if (seconds>0) items.add(new BackstageInfoResp.InfoItem(String.format("在%d场景中的体验时长",i),sceneTimeRecordService.getTimeStr(username,i)));
        }
        resp.setInformation(items);
        return resp;
    }

    @Override
    public BackstageInfoResp queryInfoByToken(String token)
    {
        BackstageInfoResp resp = new BackstageInfoResp();
        if (jwtService.checkStatus(token)!= JWTStatus.VALID)
        {
            resp.setInformation(new ArrayList<>());
            resp.setMessage(new Message(false,"token invalid or expired"));
        }
        else
        {
            resp = queryInfo(jwtService.getUsername(token));
        }
        return resp;
    }
}

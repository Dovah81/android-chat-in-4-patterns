package nju.androidchat.client.hw1;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import nju.androidchat.client.ClientMessage;
import nju.androidchat.client.hw1.Hw1Contract;

@AllArgsConstructor
public class Hw1TalkPresenter implements nju.androidchat.client.hw1.Hw1Contract.Presenter {

    private nju.androidchat.client.hw1.Hw1Contract.Model hw1TalkModel;
    private Hw1Contract.View iHw1TalkView;

    @Getter
    private List<ClientMessage> clientMessages;

    @Override
    public void sendMessage(String content) {
        ClientMessage clientMessage = hw1TalkModel.sendInformation(content);
        refreshMessageList(clientMessage);
    }

    @Override
    public void receiveMessage(ClientMessage clientMessage) {
        refreshMessageList(clientMessage);
    }

    @Override
    public String getUsername() {
        return hw1TalkModel.getUsername();
    }

    private void refreshMessageList(ClientMessage clientMessage) {
        clientMessages.add(clientMessage);
        iHw1TalkView.showMessageList(clientMessages);
    }

    //撤回消息，Mvp0暂不实现
    @Override
    public void recallMessage(int index0) {

    }

    @Override
    public void start() {

    }
}

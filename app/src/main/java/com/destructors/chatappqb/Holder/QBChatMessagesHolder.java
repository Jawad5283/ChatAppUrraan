package com.destructors.chatappqb.Holder;

import com.quickblox.chat.model.QBChatMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class QBChatMessagesHolder {

    private static QBChatMessagesHolder instance;
    private HashMap<String, ArrayList<QBChatMessage>> qbChatMessageArray;

    public static synchronized QBChatMessagesHolder getInstance()
    {
        QBChatMessagesHolder qbChatMessagesHolder;
        synchronized (QBChatMessagesHolder.class)
        {
            if (instance==null)
                instance = new QBChatMessagesHolder();
            qbChatMessagesHolder = instance;
        }
        return qbChatMessagesHolder;
    }

    private QBChatMessagesHolder()
    {
        this.qbChatMessageArray = new HashMap<>();
    }
    public void putMessages(String dialogId,ArrayList<QBChatMessage> qbChatMessages)
    {
        this.qbChatMessageArray.put(dialogId,qbChatMessages);
    }

    public void putMessage(String dialogId,QBChatMessage qbChatMessage)
    {
        List<QBChatMessage> listResult = this.qbChatMessageArray.get(dialogId);
        listResult.add(qbChatMessage);
        ArrayList<QBChatMessage> listAdded = new ArrayList(listResult.size());
        listAdded.addAll(listResult);
        putMessages(dialogId,listAdded);
    }

    public ArrayList<QBChatMessage> getChatMessagesByDialogId(String dialogId)
    {
        return (ArrayList<QBChatMessage>)this.qbChatMessageArray.get(dialogId);
    }
}

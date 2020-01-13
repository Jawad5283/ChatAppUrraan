package com.destructors.chatappqb.Common;

import com.destructors.chatappqb.Holder.QBUserHolder;
import com.quickblox.users.model.QBUser;

import java.util.List;

public class Common {

    public static final String DIALOGE_EXTRA = "Dialogs";

    public static final String UPDATE_DIALOG_EXTRA = "chatDialogs";
    public static final String UPDATE_ADD_MODE = "add";
    public static final String UPDATE_REMOVE_MODE = "remove";
    public static final String UPDATE_MODE = "Mode";

    public static String createChatDialogName(List<Integer> qbUsers)
    {
        List<QBUser> qbUsers1 = QBUserHolder.getInstance().getUsersByIds(qbUsers);
        StringBuilder name = new StringBuilder();
        for (QBUser user : qbUsers1)
            name.append(user.getFullName()).append(" ");
        if (name.length()>30)
            name = name.replace(30,name.length()-1,"...");
        return name.toString();
    }

    public static boolean isNullorEmptyString(String content)
    {
        return (content!=null && !content.trim().isEmpty()?false:true);
    }
}

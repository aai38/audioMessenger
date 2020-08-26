package com.github.chagall.notificationlistenerexample;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TelegramListener extends Service {
    private static String phoneNumber = "";
    private static String code = "";

    private Button sendButton;
    private Button getCLButton;

    private TextView cLView;
    private static Client client = null;
    private static TdApi.AuthorizationState authorizationState = null;
    private static final Client.ResultHandler defaultHandler = new DefaultHandler();
    private static volatile String currentPrompt = null;
    private static volatile boolean haveAuthorization = false;
    private static final Lock authorizationLock = new ReentrantLock();
    private static final Condition gotAuthorization = authorizationLock.newCondition();
    private static volatile boolean quiting = false;
    public static MainActivity mainActivity;
    public TelegramListener teleActivity = TelegramListener.this;

    private static final ConcurrentMap<Integer, TdApi.User> users = new ConcurrentHashMap<Integer, TdApi.User>();
    private static final NavigableSet<OrderedChat> mainChatList = new TreeSet<OrderedChat>();
    private static final ConcurrentMap<Long, TdApi.Chat> chats = new ConcurrentHashMap<Long, TdApi.Chat>();
    private static boolean haveFullMainChatList = false;


    private static HashMap<Long, String> contactList = new HashMap<>();;
    public static ArrayList<FailContactCalls> failCalls = new ArrayList<>();
    public static ArrayList<TdApi.Message> newMessages = new ArrayList<>();
    public static TdApi.Message lastMessage = null;
    private static AlertDialog dialog;
    private static ArrayList<ReceivedMessage> summarizedList = new ArrayList<>();

    private static boolean authorizationError = false;


    public static void initialize() {
        newMessages = new ArrayList<>();
        newMessages.clear();
        client = Client.create(new UpdatesHandler(), null, null);
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //dialog = new AlertDialog.Builder(mainActivity).create();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void updateFailCalls(long id) {
        FailContactCalls f = null;
        for (int i =0; i < failCalls.size(); i++) {
            if(failCalls.get(i).id == id) {
                f = failCalls.get(i);
            }
        }
        failCalls.remove(f);
        failCalls.get(failCalls.size()-1).id = id;


        mainActivity.editor =  mainActivity.shared.edit();
        Gson gson = new Gson();

        String jsonFailCalls = gson.toJson(failCalls);
        mainActivity.editor.putString("failCalls", jsonFailCalls);
        mainActivity.editor.apply();
    }

    public static void sendMessage(String msg, long id) {

        System.out.println("SendMsg to:"+id+" "+contactList.get(id));
        //System.out.println(msg);

        if(id != 0){
            sendMessage(id,msg);
        }

    }

    public static String getContactById(long id) {
        return contactList.get(id);
    }


    public static Long checkContacts(String name) {
        if(contactList.isEmpty()){
            getMainChatList(100);
            getContacts();
        }
        Long result = 0L;
        double oldSimilarity = 0;
        for (Long id: contactList.keySet()) {
            if(name.equals(contactList.get(id))) {
                return id;
            }
            double similarity = similarity(name,contactList.get(id));
            if(similarity > 0.3 && similarity > oldSimilarity) {
                oldSimilarity = similarity;
                result = id;

            }


        }
        for (FailContactCalls f: failCalls) {
            if(name.equals(f.fail1) || name.equals(f.fail2) || name.equals(f.fail3)) {
                return f.id;
            }
            double similarity = similarity(name,f.fail1);
            if(similarity > 0.3 && similarity > oldSimilarity) {
                oldSimilarity = similarity;
                result = f.id;
            }
            similarity = similarity(name,f.fail2);
            if(similarity > 0.3 && similarity > oldSimilarity) {
                oldSimilarity = similarity;
                result = f.id;
            }
            similarity = similarity(name,f.fail3);
            if(similarity > 0.3 && similarity > oldSimilarity) {
                oldSimilarity = similarity;
                result = f.id;
            }
        }

        return result;
    }




    public static double similarity(String s1, String s2) {
        String longer = s1, shorter = s2;
        if (s1.length() < s2.length()) { // longer should always have greater length
            longer = s2; shorter = s1;
        }
        int longerLength = longer.length();
        if (longerLength == 0) { return 1.0; /* both strings are zero length */ }

        return (longerLength - editDistance(longer, shorter)) / (double) longerLength;

    }


    private static int editDistance(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();

        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0)
                    costs[j] = j;
                else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1))
                            newValue = Math.min(Math.min(newValue, lastValue),
                                    costs[j]) + 1;
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0)
                costs[s2.length()] = lastValue;
        }
        return costs[s2.length()];
    }



    private static void getContacts() {
        client.send(new TdApi.GetContacts(), new UpdatesHandler());
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (Integer id : users.keySet()) {
            boolean alreadyAdded = false;
            for (Long l : contactList.keySet()){
                alreadyAdded = id.equals(l.intValue());

            }
            if(!alreadyAdded) {
                contactList.put(id.longValue(),users.get(id).firstName);
            }

        }
    }



    public static void playNextMessage(boolean resumeMessages) {
        if(!newMessages.isEmpty() && !resumeMessages){
            lastMessage = newMessages.get(0);
            newMessages.remove(0);
        }

        if(!newMessages.isEmpty()) {
           getAndPlayMessage(newMessages.get(0),false);
        }
    }

    private static void addElementToSummarizedList(TdApi.Message msg){
        boolean isAnimation = false;
        boolean isSticker = false;
        String content = msg.content.toString();
        if( content.contains("MessageSticker")) {
            isSticker = true;
        }
        if( content.contains("MessageAnimation")) {
            isAnimation = true;
        }

        Pattern pattern = Pattern.compile("text = \"((.|\\n)*)\"");



        Matcher matcher = pattern.matcher(content);
        boolean isAlreadyInList = false;
        while (matcher.find()) {
            content = matcher.group(1);
        }
        String group = contactList.get(msg.chatId);
        String person = contactList.get((long)msg.senderUserId);
        if(group == null || person == null || group.equals("null") || person.equals("null")) {
            getMainChatList(100);
            getContacts();
            group = contactList.get(msg.chatId);
            person = contactList.get((long)msg.senderUserId);
        }
        if(group.contains("Telegram")) {
            return;
        }
        for (ReceivedMessage rec: summarizedList) {
            if(rec.getPersons().get(0).equals(group)) {
                if(isSticker) {
                    rec.addText("Ein Sticker. ");
                } else if(isAnimation) {
                    rec.addText("Ein Gif. ");
                }else{
                    rec.addText(content+". ");
                }

                isAlreadyInList = true;
            } else if(rec.getGroup().equals(group)) {
                if(isSticker) {
                    rec.addText("Und "+ person +" schickt einen Sticker. ");
                } else if(isAnimation) {
                    rec.addText("Und "+ person +" schickt ein Gif. ");
                }else{
                    rec.addText("Und "+ person +" sagt§ "+content+". ");
                }

                rec.addPerson(person);
                isAlreadyInList = true;
            }
        }
        if(!isAlreadyInList) {

            summarizedList.add(new ReceivedMessage(content,person,group));
        }


    }

    public static void playStoredMessagesFromContact(String name) {

        long id = checkContacts(name);
        String contact = contactList.get(id);
        if(contact == null || contact.equals("null")) {
            getMainChatList(100);
            getContacts();
            contact = contactList.get(id);
        }
        ReceivedMessage rm = null;
        for (ReceivedMessage msg: summarizedList) {
            if(msg.getGroup().equals(contact)){
                if(msg.getPersons().get(0).equals(contact)) {
                    mainActivity.updateOutput("Nachricht von " + msg.getGroup() + "§ " + msg.getMessageText(),false,0);
                } else {
                    mainActivity.updateOutput("Nachrichten in Gruppe " + msg.getGroup() + "§ " + msg.getPersons().get(0) + " sagt§ " + msg.getMessageText()+".",false,0);
                }
                rm = msg;
                break;
            }
        }
        if(rm != null) {
            summarizedList.remove(rm);
        } else {
            mainActivity.updateOutput("Keine neuen Nachrichten von dieser Person oder Gruppe vorhanden",false,0);
        }



    }

    public static void playAllStoredMessages() {


        if (summarizedList.size() == 0) {
            mainActivity.updateOutput("Keine neuen Nachrichten",false,0);
        } else if (summarizedList.size() == 1) {


            ReceivedMessage msg = summarizedList.get(0);

            if(msg.getGroup().equals(msg.getPersons().get(0))) { //single person
                mainActivity.updateOutput("Nachricht von " + msg.getPersons().get(0) + "§ " + msg.getMessageText(),false,0);
            } else { //group
                mainActivity.updateOutput("Nachricht von " + msg.getPersons().get(0) + " in " + msg.getGroup() + "§ " + msg.getMessageText(),false,0);
            }
            summarizedList.remove(msg);

        } else {
            String persons ="";
            for (ReceivedMessage message : summarizedList) {

                if (message.getPersons().size() > 1) { //group message
                    String groupMembers = "";
                    for (String p: message.getPersons()) {
                        groupMembers += p + ", ";
                    }

                    persons += groupMembers + " in " + message.getGroup() + ",";
                } else {
                    persons += message.getPersons().get(0) + ",";
                }
            }
            //remove last ,
            if (persons != null && persons.length() > 0 && persons.charAt(persons.length() - 1) == ',') {
                persons = persons.substring(0, persons.length() - 1);
            }
            mainActivity.updateOutput("Nachrichten von " + persons, false, 0);
        }
    }

    private static void getAndPlayMessage(TdApi.Message message, boolean wait) {
        String msg = message.content.toString();
        boolean isAnimation = false;
        boolean isSticker = false;

        if( msg.contains("MessageSticker")) {
            isSticker = true;
        }
        if( msg.contains("MessageAnimation")) {
            isAnimation = true;
        }

        Pattern pattern = Pattern.compile("text = \"((.|\\n)*)\"");
        Matcher matcher = pattern.matcher(msg);
        while (matcher.find()) {
            msg = matcher.group(1);
        }
        int id = message.senderUserId;
        Long chatID = message.chatId;
        String person = contactList.get((long) message.senderUserId);
        String chat = contactList.get(message.chatId);
        if(person == null || chat == null || chat.equals("null") || person.equals("null")) {

            getMainChatList(100);
            getContacts();

            person = contactList.get((long) message.senderUserId);
            chat = contactList.get(message.chatId);
        }
        boolean isSamePerson = lastMessage != null && lastMessage.chatId == message.chatId;
        if (!wait) {
            if(id == (int) message.chatId) {
                //single chat
                if(isSamePerson) {
                    if(isSticker) {
                        mainActivity.updateOutput(person + " hat dir einen Sticker geschickt.",true, chatID);
                    } else if(isAnimation){
                        mainActivity.updateOutput(person + " hat dir ein Gif geschickt." ,true, chatID);
                    } else {
                        mainActivity.updateOutput("Nachricht von " + person + "§" + msg ,true, chatID);
                    }


                } else {
                    if(isSticker) {
                        mainActivity.updateOutput(person + " hat dir einen Sticker geschickt.",true, chatID);
                    } else if(isAnimation){
                        mainActivity.updateOutput(person + " hat dir ein Gif geschickt." ,true, chatID);
                    } else {
                        mainActivity.updateOutput("Nachricht von " + person + "§" + msg ,true, chatID);
                    }

                }
            } else {
                //group chat
                if(isSticker) {
                    mainActivity.updateOutput(person + " hat dir einen Sticker in "+chat+" geschickt.",true, chatID);
                } else if(isAnimation){
                    mainActivity.updateOutput(person + " hat dir ein Gif in "+chat+" geschickt." ,true, chatID);
                } else {
                    mainActivity.updateOutput("Nachricht von " + person + " in " + chat + "§ " + msg,true, chatID);
                }


            }
        }
    }

    private static void handleNewMessage(TdApi.UpdateNewMessage message) {

        MainActivity.isActiveMode = MainActivity.isActiveModeSwitch.isChecked();
        if(MainActivity.isActiveMode) {
            boolean wait = !newMessages.isEmpty() || MainActivity.isBusy;
            newMessages.add(message.message);
            getAndPlayMessage(message.message,wait);
            lastMessage = message.message;
        } else {

            addElementToSummarizedList(message.message);
        }
    }


    private static void sendMessage(long chatId, String message) {
        // initialize reply markup just for testing
        TdApi.InlineKeyboardButton[] row = {new TdApi.InlineKeyboardButton("https://telegram.org?1", new TdApi.InlineKeyboardButtonTypeUrl()), new TdApi.InlineKeyboardButton("https://telegram.org?2", new TdApi.InlineKeyboardButtonTypeUrl()), new TdApi.InlineKeyboardButton("https://telegram.org?3", new TdApi.InlineKeyboardButtonTypeUrl())};
        TdApi.ReplyMarkup replyMarkup = new TdApi.ReplyMarkupInlineKeyboard(new TdApi.InlineKeyboardButton[][]{row, row, row});

        TdApi.InputMessageContent content = new TdApi.InputMessageText(new TdApi.FormattedText(message, null), false, true);
        client.send(new TdApi.SendMessage(chatId, 0, null, replyMarkup, content), defaultHandler);
    }

    private static class DefaultHandler implements Client.ResultHandler {
        @Override
        public void onResult(TdApi.Object object) {
        }
    }

    private static class UpdatesHandler implements Client.ResultHandler {
        @Override
        public void onResult(TdApi.Object object) {
            switch (object.getConstructor()) {
                case TdApi.UpdateNewMessage.CONSTRUCTOR:

                    TdApi.UpdateNewMessage updateNewMessage = (TdApi.UpdateNewMessage) object;

                    if(!updateNewMessage.message.isOutgoing) {
                        Long id = updateNewMessage.message.chatId;
                        if(chats.get(id) != null && contactList.get(id) == null) {
                            boolean alreadyAdded = false;
                            for (Long l : contactList.keySet()){
                                alreadyAdded = id.equals(l);

                            }
                            if(!alreadyAdded) {
                                contactList.put(id, Objects.requireNonNull(chats.get(id)).title);
                            }
                        }
                        handleNewMessage(updateNewMessage);
                    }


                    break;
                case TdApi.UpdateAuthorizationState.CONSTRUCTOR:
                    onAuthorizationStateUpdated(((TdApi.UpdateAuthorizationState) object).authorizationState);
                    break;
                case TdApi.UpdateUser.CONSTRUCTOR:
                    TdApi.UpdateUser updateUser = (TdApi.UpdateUser) object;
                    users.put(updateUser.user.id, updateUser.user);
                    break;
                case TdApi.UpdateUserStatus.CONSTRUCTOR: {
                    TdApi.UpdateUserStatus updateUserStatus = (TdApi.UpdateUserStatus) object;
                    TdApi.User user = users.get(updateUserStatus.userId);
                    synchronized (user) {
                        user.status = updateUserStatus.status;
                    }
                    break;
                }
                case TdApi.UpdateBasicGroup.CONSTRUCTOR:
                    TdApi.UpdateBasicGroup updateBasicGroup = (TdApi.UpdateBasicGroup) object;
                    //basicGroups.put(updateBasicGroup.basicGroup.id, updateBasicGroup.basicGroup);
                    break;
                case TdApi.UpdateSupergroup.CONSTRUCTOR:
                    TdApi.UpdateSupergroup updateSupergroup = (TdApi.UpdateSupergroup) object;
                    //supergroups.put(updateSupergroup.supergroup.id, updateSupergroup.supergroup);
                    break;
                case TdApi.UpdateSecretChat.CONSTRUCTOR:
                    TdApi.UpdateSecretChat updateSecretChat = (TdApi.UpdateSecretChat) object;
                    //secretChats.put(updateSecretChat.secretChat.id, updateSecretChat.secretChat);
                    break;

                case TdApi.UpdateNewChat.CONSTRUCTOR: {
                    TdApi.UpdateNewChat updateNewChat = (TdApi.UpdateNewChat) object;

                    TdApi.Chat chat = updateNewChat.chat;

                    synchronized (chat) {
                        chats.put(chat.id, chat);

                        long order = chat.order;
                        chat.order = 0;
                        setChatOrder(chat, order);
                        Long id = chat.id;
                        if(chats.get(id) != null && contactList.get(id) == null) {
                            boolean alreadyAdded = false;
                            for (Long l : contactList.keySet()){
                                alreadyAdded = id.equals(l);

                            }
                            if(!alreadyAdded) {

                                contactList.put(id, Objects.requireNonNull(chats.get(id)).title);


                            }
                        }

                    }
                    break;
                }
                case TdApi.UpdateChatTitle.CONSTRUCTOR: {
                    TdApi.UpdateChatTitle updateChat = (TdApi.UpdateChatTitle) object;
                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        chat.title = updateChat.title;
                    }
                    break;
                }
                case TdApi.UpdateChatPhoto.CONSTRUCTOR: {
                    TdApi.UpdateChatPhoto updateChat = (TdApi.UpdateChatPhoto) object;
                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        chat.photo = updateChat.photo;
                    }
                    break;
                }
                case TdApi.UpdateChatChatList.CONSTRUCTOR: {

                    TdApi.UpdateChatChatList updateChat = (TdApi.UpdateChatChatList) object;
                    TdApi.Chat chat = chats.get(updateChat.chatId);

                    synchronized (mainChatList) { // to not change Chat.chatList while mainChatList is locked
                        synchronized (chat) {
                            assert chat.order == 0; // guaranteed by TDLib
                            chat.chatList = updateChat.chatList;
                            Long id = updateChat.chatId;
                            if(chats.get(id) != null && contactList.get(id) == null) {
                                boolean alreadyAdded = false;
                                for (Long l : contactList.keySet()){
                                    alreadyAdded = id.equals(l);

                                }
                                if(!alreadyAdded) {
                                    contactList.put(id, Objects.requireNonNull(chats.get(id)).title);
                                }
                            }
                        }
                    }
                    break;
                }
                case TdApi.UpdateChatLastMessage.CONSTRUCTOR: {
                    /*TdApi.UpdateChatLastMessage updateChat = (TdApi.UpdateChatLastMessage) object;
                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        chat.lastMessage = updateChat.lastMessage;
                        setChatOrder(chat, updateChat.order);
                    }*/
                    break;
                }
                case TdApi.UpdateChatOrder.CONSTRUCTOR: {
                    TdApi.UpdateChatOrder updateChat = (TdApi.UpdateChatOrder) object;
                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {

                        setChatOrder(chat, updateChat.order);
                        Long id = updateChat.chatId;
                        if(chats.get(id) != null && contactList.get(id) == null) {
                            boolean alreadyAdded = false;
                            for (Long l : contactList.keySet()){
                                alreadyAdded = id.equals(l);

                            }
                            if(!alreadyAdded) {
                                contactList.put(id, Objects.requireNonNull(chats.get(id)).title);
                            }
                        }
                    }
                    break;
                }
                case TdApi.UpdateChatIsPinned.CONSTRUCTOR: {
                    TdApi.UpdateChatIsPinned updateChat = (TdApi.UpdateChatIsPinned) object;
                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        chat.isPinned = updateChat.isPinned;
                        setChatOrder(chat, updateChat.order);
                    }
                    break;
                }
                case TdApi.UpdateChatReadInbox.CONSTRUCTOR: {
                    TdApi.UpdateChatReadInbox updateChat = (TdApi.UpdateChatReadInbox) object;
                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        chat.lastReadInboxMessageId = updateChat.lastReadInboxMessageId;
                        chat.unreadCount = updateChat.unreadCount;
                    }
                    break;
                }
                case TdApi.UpdateChatReadOutbox.CONSTRUCTOR: {
                    TdApi.UpdateChatReadOutbox updateChat = (TdApi.UpdateChatReadOutbox) object;
                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        chat.lastReadOutboxMessageId = updateChat.lastReadOutboxMessageId;
                    }
                    break;
                }
                case TdApi.UpdateChatUnreadMentionCount.CONSTRUCTOR: {
                    TdApi.UpdateChatUnreadMentionCount updateChat = (TdApi.UpdateChatUnreadMentionCount) object;
                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        chat.unreadMentionCount = updateChat.unreadMentionCount;
                    }
                    break;
                }
                case TdApi.UpdateMessageMentionRead.CONSTRUCTOR: {
                    TdApi.UpdateMessageMentionRead updateChat = (TdApi.UpdateMessageMentionRead) object;
                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        chat.unreadMentionCount = updateChat.unreadMentionCount;
                    }
                    break;
                }
                case TdApi.UpdateChatReplyMarkup.CONSTRUCTOR: {
                    TdApi.UpdateChatReplyMarkup updateChat = (TdApi.UpdateChatReplyMarkup) object;
                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        chat.replyMarkupMessageId = updateChat.replyMarkupMessageId;
                    }
                    break;
                }
                case TdApi.UpdateChatDraftMessage.CONSTRUCTOR: {
                    TdApi.UpdateChatDraftMessage updateChat = (TdApi.UpdateChatDraftMessage) object;
                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        chat.draftMessage = updateChat.draftMessage;
                        setChatOrder(chat, updateChat.order);
                    }
                    break;
                }
                case TdApi.UpdateChatNotificationSettings.CONSTRUCTOR: {
                    TdApi.UpdateChatNotificationSettings update = (TdApi.UpdateChatNotificationSettings) object;
                    TdApi.Chat chat = chats.get(update.chatId);
                    synchronized (chat) {
                        chat.notificationSettings = update.notificationSettings;
                    }
                    break;
                }
                case TdApi.UpdateChatDefaultDisableNotification.CONSTRUCTOR: {
                    TdApi.UpdateChatDefaultDisableNotification update = (TdApi.UpdateChatDefaultDisableNotification) object;
                    TdApi.Chat chat = chats.get(update.chatId);
                    synchronized (chat) {
                        chat.defaultDisableNotification = update.defaultDisableNotification;
                    }
                    break;
                }
                case TdApi.UpdateChatIsMarkedAsUnread.CONSTRUCTOR: {
                    TdApi.UpdateChatIsMarkedAsUnread update = (TdApi.UpdateChatIsMarkedAsUnread) object;
                    TdApi.Chat chat = chats.get(update.chatId);
                    synchronized (chat) {
                        chat.isMarkedAsUnread = update.isMarkedAsUnread;
                    }
                    break;
                }
                case TdApi.UpdateChatIsSponsored.CONSTRUCTOR: {
                    TdApi.UpdateChatIsSponsored updateChat = (TdApi.UpdateChatIsSponsored) object;
                    TdApi.Chat chat = chats.get(updateChat.chatId);
                    synchronized (chat) {
                        chat.isSponsored = updateChat.isSponsored;
                        setChatOrder(chat, updateChat.order);
                    }
                    break;
                }

                case TdApi.UpdateUserFullInfo.CONSTRUCTOR:
                    TdApi.UpdateUserFullInfo updateUserFullInfo = (TdApi.UpdateUserFullInfo) object;
                    //usersFullInfo.put(updateUserFullInfo.userId, updateUserFullInfo.userFullInfo);
                    break;
                case TdApi.UpdateBasicGroupFullInfo.CONSTRUCTOR:
                    TdApi.UpdateBasicGroupFullInfo updateBasicGroupFullInfo = (TdApi.UpdateBasicGroupFullInfo) object;
                    //basicGroupsFullInfo.put(updateBasicGroupFullInfo.basicGroupId, updateBasicGroupFullInfo.basicGroupFullInfo);
                    break;
                case TdApi.UpdateSupergroupFullInfo.CONSTRUCTOR:
                    TdApi.UpdateSupergroupFullInfo updateSupergroupFullInfo = (TdApi.UpdateSupergroupFullInfo) object;
                    //supergroupsFullInfo.put(updateSupergroupFullInfo.supergroupId, updateSupergroupFullInfo.supergroupFullInfo);
                    break;
                case TdApi.UpdateConnectionState.CONSTRUCTOR:
                    TdApi.UpdateConnectionState connectionState = (TdApi.UpdateConnectionState) object;
                    String text = "";
                    switch(connectionState.state.getConstructor()){
                        case TdApi.ConnectionStateConnecting.CONSTRUCTOR: //currently establishing a connection
                            text = "Keine Verbindung zum Telegram Server";
                            mainActivity.showServerConnectionStatus(text);
                            break;
                        case TdApi.ConnectionStateReady.CONSTRUCTOR: //there is a working connection
                            text = "Verbindung zum Telegram Server hergestellt";
                            mainActivity.showServerConnectionStatus(text);
                            break;
                        case TdApi.ConnectionStateWaitingForNetwork.CONSTRUCTOR: //currently waiting for the network to become available
                            text = "Auf Verbindung zum Telegram Server warten...";
                            mainActivity.showServerConnectionStatus(text);
                            break;
                        default:
                            break;
                    }

                    break;
                default:
                    System.out.println("Unsupported update:" + object);
            }
        }
    }

    private static class AuthorizationRequestHandler implements Client.ResultHandler {
        boolean authorized = false;
        @Override
        public void onResult(TdApi.Object object) {
            switch (object.getConstructor()) {
                case TdApi.Error.CONSTRUCTOR:
                    System.out.println("FEHLER BEI DER AUTORISIERUNG: "+object);
                    authorizationError = true;
                    Toast.makeText(mainActivity, "Fehler: "+object, Toast.LENGTH_LONG).show();
                    if(!authorized) {
                        onAuthorizationStateUpdated(null); // repeat last action
                    }

                    break;
                case TdApi.Ok.CONSTRUCTOR:
                    System.out.println("AUTORISIERUNG GUT"+ object);
                    authorizationError = false;
                    Toast.makeText(mainActivity, "Erfolgreich", Toast.LENGTH_LONG).show();
                    authorized = true;
                    // result is already received through UpdateAuthorizationState, nothing to do
                    break;
                default:
                    //System.err.println("Receive wrong response from TDLib:" + newLine + object);
            }
        }
    }

    private static class OrderedChat implements Comparable<OrderedChat> {
        final long order;
        final long chatId;

        OrderedChat(long order, long chatId) {
            this.order = order;
            this.chatId = chatId;
        }

        @Override
        public int compareTo(OrderedChat o) {
            if (this.order != o.order) {
                return o.order < this.order ? -1 : 1;
            }
            if (this.chatId != o.chatId) {
                return o.chatId < this.chatId ? -1 : 1;
            }
            return 0;
        }

        @Override
        public boolean equals(Object obj) {
            OrderedChat o = (OrderedChat) obj;
            return this.order == o.order && this.chatId == o.chatId;
        }
    }

    private static void onAuthorizationStateUpdated(TdApi.AuthorizationState authorizationState) {
        if (authorizationState != null) {
            TelegramListener.authorizationState = authorizationState;
        }
        switch (TelegramListener.authorizationState.getConstructor()) {
            case TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR:
                TdApi.TdlibParameters parameters = new TdApi.TdlibParameters();
                parameters.databaseDirectory = mainActivity.getApplicationContext().getFilesDir().getAbsolutePath();
                parameters.useMessageDatabase = true;
                parameters.useSecretChats = true;
                //parameters.apiId = 1216467;
                parameters.apiId = 94575;
                //parameters.apiHash = "54970baa9768502b0ab38710e109affd";
                parameters.apiHash = "a3406de8d171bb422bb6ddf3bbd800e2";
                parameters.systemLanguageCode = "en";
                parameters.deviceModel = "Desktop";
                parameters.systemVersion = "Unknown";
                parameters.applicationVersion = "1.0";
                parameters.enableStorageOptimizer = true;

                client.send(new TdApi.SetTdlibParameters(parameters), new AuthorizationRequestHandler());
                break;
            case TdApi.AuthorizationStateWaitEncryptionKey.CONSTRUCTOR:
                client.send(new TdApi.CheckDatabaseEncryptionKey(), new AuthorizationRequestHandler());
                break;
            case TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR: {
                showAuthorizationView();
                break;
            }
            case TdApi.AuthorizationStateWaitOtherDeviceConfirmation.CONSTRUCTOR: {
                String link = ((TdApi.AuthorizationStateWaitOtherDeviceConfirmation) TelegramListener.authorizationState).link;
                System.out.println("Please confirm this login link on another device: " + link);
                break;
            }
            case TdApi.AuthorizationStateWaitCode.CONSTRUCTOR: {
                //String code = promptString("Please enter authentication code: ");
                //client.send(new TdApi.CheckAuthenticationCode(code), new AuthorizationRequestHandler());
                showAuthorizationView();
                break;
            }
            case TdApi.AuthorizationStateWaitRegistration.CONSTRUCTOR: {
                String firstName = promptString("Please enter your first name: ");
                String lastName = promptString("Please enter your last name: ");
                client.send(new TdApi.RegisterUser(firstName, lastName), new AuthorizationRequestHandler());
                break;
            }
            case TdApi.AuthorizationStateWaitPassword.CONSTRUCTOR: {
                String password = promptString("Please enter password: ");
                client.send(new TdApi.CheckAuthenticationPassword(password), new AuthorizationRequestHandler());
                break;
            }
            case TdApi.AuthorizationStateReady.CONSTRUCTOR:
                haveAuthorization = true;
                authorizationLock.lock();
                try {
                    gotAuthorization.signal();
                } finally {
                    authorizationLock.unlock();
                }

                break;
            case TdApi.AuthorizationStateLoggingOut.CONSTRUCTOR:
                haveAuthorization = false;
                //print("Logging out");
                break;
            case TdApi.AuthorizationStateClosing.CONSTRUCTOR:
                haveAuthorization = false;
                //print("Closing");
                break;
            case TdApi.AuthorizationStateClosed.CONSTRUCTOR:
                //print("Closed");
                if (!quiting) {
                    client = Client.create(new UpdatesHandler(), null, null); // recreate client after previous has closed
                }
                break;
            default:
                //System.err.println("Unsupported authorization state:" + newLine + Example.authorizationState);
        }
    }

    private static String promptString(String prompt) {
        System.out.print(prompt);
        currentPrompt = prompt;
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String str = "";
        try {
            str = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        currentPrompt = null;
        return str;
    }
    private static int counter = 10;
    private static void getMainChatList(final int limit) {
       if(counter > 0) {
           final HashMap<Long, String> currentMap = new HashMap<>();
           synchronized (mainChatList) {

               if (!haveFullMainChatList && limit > mainChatList.size()) {
                   // have enough chats in the chat list or chat list is too small
                   long offsetOrder = Long.MAX_VALUE;
                   long offsetChatId = 0;
                   if (!mainChatList.isEmpty()) {
                       OrderedChat last = mainChatList.last();
                       offsetOrder = last.order;
                       offsetChatId = last.chatId;
                   }
                   int chats_size = chats.size();

                   client.send(new TdApi.GetChats(new TdApi.ChatListMain(), offsetOrder, offsetChatId, limit - mainChatList.size()), new Client.ResultHandler() {
                       @Override
                       public void onResult(TdApi.Object object) {
                           switch (object.getConstructor()) {
                               case TdApi.Error.CONSTRUCTOR:

                                   break;
                               case TdApi.Chats.CONSTRUCTOR:
                                   long[] chatIds = ((TdApi.Chats) object).chatIds;

                                   if (chatIds.length == 0) {
                                       synchronized (mainChatList) {
                                           haveFullMainChatList = true;
                                       }
                                   }
                                   // chats had already been received through updates, let's retry request

                                   getMainChatList(limit);
                                   counter--;
                                   break;
                               default:
                                   //System.err.println("Receive wrong response from TDLib:" + newLine + object);
                           }
                       }
                   });
                   try {
                       TimeUnit.SECONDS.sleep(2);
                   } catch (InterruptedException e) {
                       e.printStackTrace();
                   }
                   for (Long l : chats.keySet()) {
                       if (contactList.get(l) == null) {
                           boolean alreadyAdded = false;
                           for (Long le : contactList.keySet()) {
                               alreadyAdded = l.equals(le);

                           }
                           if (!alreadyAdded) {
                               contactList.put(l, Objects.requireNonNull(chats.get(l)).title);
                           }
                       }

                   }
                   //return currentMap;
                   return;
               }

               // have enough chats in the chat list to answer request

               java.util.Iterator<OrderedChat> iter = mainChatList.iterator();
               //System.out.println();
               //System.out.println("First " + limit + " chat(s) out of " + mainChatList.size() + " known chat(s):");
               for (int i = 0; i < limit; i++) {
                   if (iter.hasNext()) {
                       long chatId = iter.next().chatId;
                       TdApi.Chat chat = chats.get(chatId);
                       synchronized (chat) {
                           currentMap.put(chatId, chat.title);
                           contactList = currentMap;
                       }
                   }

               }
               contactList = currentMap;


           }
       } else {
           counter = 10;
       }

    }

    private static void setChatOrder(TdApi.Chat chat, long order) {
        synchronized (mainChatList) {
            synchronized (chat) {
                if (chat.chatList == null || chat.chatList.getConstructor() != TdApi.ChatListMain.CONSTRUCTOR) {
                    return;
                }

                if (chat.order != 0) {
                    boolean isRemoved = mainChatList.remove(new OrderedChat(chat.order, chat.id));
                    assert isRemoved;
                }

                chat.order = order;

                if (chat.order != 0) {
                    boolean isAdded = mainChatList.add(new OrderedChat(chat.order, chat.id));
                    assert isAdded;
                }
            }
        }
    }

    private static void showAuthorizationView(){
        mainActivity.setContentView(R.layout.authorization_phone_number);
        EditText input = (EditText) mainActivity.findViewById(R.id.phoneNumber);
        Button phoneButton = (Button) mainActivity.findViewById(R.id.phoneButton);
        phoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(input.getText().toString().isEmpty()){
                    Toast.makeText(mainActivity, "Vervollständige bitte die Telefonnummer.",
                            Toast.LENGTH_LONG).show();
                } else {
                    phoneNumber = "+49"+input.getText().toString().replaceAll("\\s+",""); //remove whitespaces
                    client.send(new TdApi.SetAuthenticationPhoneNumber(phoneNumber, null), new AuthorizationRequestHandler());
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(!authorizationError){ //no error
                        mainActivity.setContentView(R.layout.authorization_login_code);
                        ImageView backButton = mainActivity.findViewById(R.id.backButtonNumber);
                        EditText inputCode = (EditText) mainActivity.findViewById(R.id.loginCode);
                        Button codeButton = (Button) mainActivity.findViewById(R.id.codeButton);
                        backButton.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {
                                showAuthorizationView();
                            }
                        });
                        codeButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(inputCode.getText().toString().isEmpty()){
                                    Toast.makeText(mainActivity, "Gib bitte einen Login Code ein.",
                                            Toast.LENGTH_LONG).show();
                                } else {
                                    code = inputCode.getText().toString();
                                    client.send(new TdApi.CheckAuthenticationCode(code), new AuthorizationRequestHandler());
                                    try {
                                        TimeUnit.SECONDS.sleep(1);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    if(!authorizationError){
                                        mainActivity.setContentView(R.layout.activity_main);
                                        mainActivity.activateButtons();
                                    }
                                }

                            }
                        });
                    }

                }
            }
        });
    }



    public static HashMap<Long, String> getContactList () {
        if(contactList.isEmpty()) {
            getContacts();
            getMainChatList(100);
        }

        return contactList;
    }


}

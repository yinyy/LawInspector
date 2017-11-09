package kl.law.inspector.tools;

/**
 * Created by yinyy on 2017/10/9.
 */

public class UserData {
    private String username;
    private String id;
    private String name;
    private String officeId;
    private String officeName;
    private String no;
    private String channelId;

    private static UserData user;

    private UserData(){

    }

    public static UserData getInstance(){
        if(user==null){
            user = new UserData();
        }

        return user;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOfficeId() {
        return officeId;
    }

    public void setOfficeId(String officeId) {
        this.officeId = officeId;
    }

    public String getOfficeName() {
        return officeName;
    }

    public void setOfficeName(String officeName) {
        this.officeName = officeName;
    }

    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

}
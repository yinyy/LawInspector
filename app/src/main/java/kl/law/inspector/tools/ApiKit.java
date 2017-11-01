package kl.law.inspector.tools;

import java.text.MessageFormat;

/**
 * Created by yinyy on 2017/8/17.
 */

public class ApiKit {
    //private static final String API_URL_PREFIX = "http://47.93.52.62:8080/klzf2";
    //private static final String API_URL_PREFIX = "http://192.168.31.205:8080/day5";
    //private static final String API_URL_PREFIX = "http://10.0.2.2:8080/law2";
    private static final String API_URL_PREFIX = "http://10.0.2.2:8080/day5";

    public static final class ArticleCategory{
        /**
         * 案件来源
         */
        public static final String LEGAL_CASE_SOURCE = "0cbf63e927174b94bcbe19b6d541a19e";
        /**
         * 违法行为
         */
        public static final String ILLEGAL_BEHAVIOR = "3";
        /**
         * 处罚办法
         */
        public static final String PUNISH = "4";
        /**
         * 法律条文
         */
        public static final String LEGAL_PROVISION = "5";
        /**
         * 通知分类
         */
        public static final String NOTIFICATION = "360078dd9a304fecaafdf077d95ce287";
    }

    public static String URL_UPLOAD_FILE = MessageFormat.format("{0}/apiv1/oa/files/upload", API_URL_PREFIX);
    public static String URL_USER= MessageFormat.format("{0}/apiv1/user/info/list", API_URL_PREFIX);
    public static String URL_LEGAL_CASE_CREATE = MessageFormat.format("{0}/apiv1/oa/case/create", API_URL_PREFIX);
    public static String URL_LEGAL_CASE_APPROVE = MessageFormat.format("{0}/apiv1/oa/case/approve", API_URL_PREFIX);
    public static String URL_LEGAL_CASE_FILE_FINISHED = MessageFormat.format("{0}/apiv1/oa/case/filefinished", API_URL_PREFIX);
    public static String URL_LEGAL_CASE_UPDATE_FILES = MessageFormat.format("{0}/apiv1/oa/case/updatefiles", API_URL_PREFIX);
    public static String URL_DOCUMENT_CREATE = MessageFormat.format("{0}/apiv1/oa/document/create", API_URL_PREFIX);
    public static String URL_DOCUMENT_APPROVE = MessageFormat.format("{0}/apiv1/oa/document/approve", API_URL_PREFIX);


    public static String URL_LOGIN(String username, String password){
        return MessageFormat.format("{0}/apiv1/user/info/login?login_name={1}&password={2}", API_URL_PREFIX, username, password);
    }

    public static String URL_ARTICLE(String categoryid){
        return MessageFormat.format("{0}/apiv1/cms/article/list?categoryid={1}", API_URL_PREFIX, categoryid);
    }

    public static String URL_USER(String officeId){
        return MessageFormat.format("{0}/apiv1/user/info/list?office_id={1}", API_URL_PREFIX, officeId);
    }

    public static String URL_USER(String officeId, String subOfficeName){
        return MessageFormat.format("{0}/apiv1/user/info/list?office_id={1}&child_office_name={2}", API_URL_PREFIX, officeId, subOfficeName);
    }

    public static String URL_LEGAL_CASE_LIST(int page, int stage, String user_id){
        return MessageFormat.format("{0}/apiv1/oa/case/list?page={1}&stage={2}&user_id={3}",API_URL_PREFIX, page, stage, user_id);
    }

    public static String URL_LEGAL_CASE_TODO_LIST(String user_id){
        return MessageFormat.format("{0}/apiv1/oa/case/todolist?user_id={1}", API_URL_PREFIX, user_id);
    }

    public static String URL_REMINDER_TODO_LIST(String user_id){
        return MessageFormat.format("{0}/apiv1/oa/case/todolist?user_id={1}", API_URL_PREFIX, user_id);
    }

    public static String URL_LEGAL_CASE_DETAIL(String legalCaseId){
        return MessageFormat.format("{0}/apiv1/oa/case/get?id={1}", API_URL_PREFIX, legalCaseId);
    }

    public static String URL_LEGAL_CASE_PROGRESS_LIST(String legalCaseId){
        return MessageFormat.format("{0}/apiv1/oa/case/getstagelist?id={1}", API_URL_PREFIX, legalCaseId);
    }

    public static String URL_LEGAL_CASE_FILE_LIST(String legalCaseId){
        return MessageFormat.format("{0}/apiv1/oa/case/getfile?id={1}", API_URL_PREFIX, legalCaseId);
    }

    public static String URL_DOCUMENT_TODO_LIST(String user_id){
        return MessageFormat.format("{0}/apiv1/oa/case/todolist?user_id={1}", API_URL_PREFIX, user_id);
    }

    public static String URL_DOCUMENT_LIST(int page, int stage, String user_id){
        return MessageFormat.format("{0}/apiv1/oa/document/list?page={1}&stage={2}&user_id={3}",API_URL_PREFIX, page, stage, user_id);
    }

    public static String URL_DOCUMENT_FILE_LIST(String documentId){
        return MessageFormat.format("{0}/apiv1/oa/document/getfile?id={1}", API_URL_PREFIX, documentId);
    }

    public static String URL_DOCUMENT_DETAIL(String documentId){
        return MessageFormat.format("{0}/apiv1/oa/document/get?id={1}", API_URL_PREFIX, documentId);
    }





    //检查软件更新
    public static String URL_CHECK_UPDATE(int versionCode){
        return MessageFormat.format("{0}/apiv1/app/check/update?versionCode={1}", API_URL_PREFIX, versionCode);
    }
}

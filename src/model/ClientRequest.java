package model;

public enum ClientRequest {
   CREATE_ACCOUNT, CHANGE_PASSWORD, LOGIN, LOGOUT,
   GET_DOCS, CREATE_DOC, OPEN_DOC, CLOSE_DOC, DELETE_DOC, 
   CHAT_MSG, DOC_TEXT, SAVE_REVISION, REVERT_DOC,   
   GET_EDITORS, GET_USERS, ADD_PERMISSION, REMOVE_PERMISSION;

}

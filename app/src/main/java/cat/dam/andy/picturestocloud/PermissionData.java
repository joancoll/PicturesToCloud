package cat.dam.andy.picturestocloud;

public class PermissionData {
    // Members
    private String permission;
    private String permissionExplanation;
    private String permissionDeniedMessage;
    private String permissionGrantedMessage;
    private String permissionPermanentDeniedMessage;


    public PermissionData(String permission, String permissionExplanation, String permissionDeniedMessage, String permissionGrantedMessage, String permissionPermanentDeniedMessage) {
        this.permission = permission;
        this.permissionExplanation = permissionExplanation;
        this.permissionDeniedMessage = permissionDeniedMessage;
        this.permissionGrantedMessage = permissionGrantedMessage;
        this.permissionPermanentDeniedMessage = permissionPermanentDeniedMessage;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getpermissionExplanation() {
        return permissionExplanation;
    }

    public void setpermissionExplanation(String permissionExplanation) {
        this.permissionExplanation = permissionExplanation;
    }

    public String getpermissionDeniedMessage() {
        return permissionDeniedMessage;
    }

    public void setpermissionDeniedMessage(String permissionDeniedMessage) {
        this.permissionDeniedMessage = permissionDeniedMessage;
    }

    public String getPermissionGrantedMessage() {
        return permissionGrantedMessage;
    }

    public void setPermissionGrantedMessage(String permissionGrantedMessage) {
        this.permissionGrantedMessage = permissionGrantedMessage;
    }

    public String getpermissionPermanentDeniedMessage() {
        return permissionPermanentDeniedMessage;
    }

    public void setpermissionPermanentDeniedMessage(String permissionPermanentDeniedMessage) {
        this.permissionPermanentDeniedMessage = permissionPermanentDeniedMessage;
    }

}


package com.company.Classes;

import com.google.gson.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import static com.company.Classes.DBConnection.getConn;
import static com.company.Classes.DBConnection.getGson;

public class UserInfo {

    /*
    users_info table in MySQL database columns:
                1.  uid             int PK      (user's uid)
                2.  about           longtext    (about string)
                3.  weight          double      (weight rounded with halves)
                4.  height          double      (height rounded with halves)
                4.  birthdate       date        (user's date of birth)
                5.  relationship    int         (enum value in int)
                6.  religion        int         (enum value in int)
                7.  orientation     int         (enum value in int)
                8.  ethnicity       int         (enum value in int)
                9.  reference       int         (enum value in int)
                10. stds            longtext    (json array of enum values in int)
                11. role            int         (enum value in int)
                12. disability      longtext    (json array of enum values in int)

     */

    public static final String UID = "uid";
    public static final String ABOUT = "about";
    public static final String WEIGHT = "weight";
    public static final String HEIGHT = "height";
    public static final String BIRTH_DATE = "birthDate";
    public static final String RELATIONSHIP = "relationship";
    public static final String RELIGION = "religion";
    public static final String ORIENTATION = "orientation";
    public static final String ETHNICITY = "ethnicity";
    public static final String REFERENCE = "reference";
    public static final String STDS = "stds";
    public static final String ROLE = "role";
    public static final String DISABILITIES = "disabilities";
    private int uid;
    private String about;
    private int weight, height;
    private Date birthDate;
    private Relationship relationship;
    private Religion religion;
    private Orientation orientation;
    private Ethnicity ethnicity;
    private Reference reference;
    private STD[] stDs;
    private Role role;
    private Disability[] disabilities;
    public boolean notInDB;

    public UserInfo(int uid, String about, int weight, int height, Date birthDate, Relationship relationship, Religion religion, Orientation orientation, Ethnicity ethnicity, Reference reference, STD[] stDs, Role role, Disability[] disabilities) {
        this.uid = uid;
        this.about = about;
        this.weight = weight;
        this.height = height;
        this.birthDate = birthDate;
        this.relationship = relationship;
        this.religion = religion;
        this.orientation = orientation;
        this.ethnicity = ethnicity;
        this.reference = reference;
        this.stDs = stDs;
        this.role = role;
        this.disabilities = disabilities;
        this.notInDB = false;
    }

    public UserInfo(InputStream inputStream) throws IOException {
        int jsonLength = inputStream.read();
        if (jsonLength == -1)
            throw new IOException("json hasn't been sent");
        byte[] jsonBytes = new byte[jsonLength];
        int actuallyRead = inputStream.read(jsonBytes);
        if (actuallyRead != jsonLength)
            throw new IOException("");
        UserInfo jsonUserInfo = getUserInfoFromJson(new String(jsonBytes));
        this.uid = jsonUserInfo.getUid();
        this.about = jsonUserInfo.getAbout();
        this.weight = jsonUserInfo.getWeight();
        this.height = jsonUserInfo.getHeight();
        this.birthDate = jsonUserInfo.getBirthDate();
        this.relationship = jsonUserInfo.getRelationship();
        this.religion = jsonUserInfo.getReligion();
        this.orientation = jsonUserInfo.getOrientation();
        this.ethnicity = jsonUserInfo.getEthnicity();
        this.reference = jsonUserInfo.getReference();
        this.stDs = jsonUserInfo.getStDs();
        this.role = jsonUserInfo.getRole();
        this.disabilities = jsonUserInfo.getDisabilities();
        this.notInDB = false;
    }

    public UserInfo(int uid) {
        this.uid = uid;
        this.notInDB = true;
    }

    public UserInfo(JsonObject jsonObject) throws ParseException {
        this.uid = jsonObject.get(UID).getAsInt();
        this.about = jsonObject.get(ABOUT).getAsString();
        this.weight = jsonObject.get(WEIGHT).getAsInt();
        this.height = jsonObject.get(HEIGHT).getAsInt();
        this.birthDate = Date.valueOf(jsonObject.get(BIRTH_DATE).getAsString());
        this.relationship = Relationship.getEnumValOf(jsonObject.get(RELATIONSHIP).getAsInt());
        this.religion = Religion.getEnumValOf(jsonObject.get(RELIGION).getAsInt());
        this.orientation = Orientation.getEnumValOf(jsonObject.get(ORIENTATION).getAsInt());
        this.ethnicity = Ethnicity.getEnumValOf(jsonObject.get(ETHNICITY).getAsInt());
        this.reference = Reference.getEnumValOf(jsonObject.get(REFERENCE).getAsInt());
        JsonArray jsonArray = jsonObject.get(STDS).getAsJsonArray();
        int[] arr = new int[jsonArray.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = jsonArray.get(i).getAsInt();
        }
        this.stDs = STD.getEnumsFrom(arr);
        this.role = Role.getEnumValOf(jsonObject.get(ROLE).getAsInt());
        jsonArray = jsonObject.get(DISABILITIES).getAsJsonArray();
        arr = new int[jsonArray.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = jsonArray.get(i).getAsInt();
        }
        this.disabilities = Disability.getEnumsFrom(arr);
        this.notInDB = false;
    }

    /**
     * Get UserInfo object from Json String.
     * @param json
     * Json String.
     * @return
     * UserInfo Object.
     */
    public static UserInfo getUserInfoFromJson(String json) {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();

        Gson gson = builder.create();
        return gson.fromJson(json, UserInfo.class);
    }

    /**
     * Adds a new User. This functions communicates with the DB.
     * @param userInfo
     * New UserInfo to add.
     * @return
     * True if succeeded, false if not.
     */
    public static boolean addUserInfo(UserInfo userInfo){
        try(Connection conn = getConn()){
            boolean userAlreadyExists = false;
            try(PreparedStatement statement = conn.prepareStatement("SELECT * FROM users_info WHERE uid=?")){
                statement.setString(1, userInfo.getUid()+"");
                try(ResultSet resultSet = statement.executeQuery()){
                    if(resultSet.next())
                        userAlreadyExists = true;
                }
            }
            if(userAlreadyExists)
                return false;
            try(PreparedStatement statement = conn.prepareStatement(
                    "INSERT INTO users_info(" +
                            "uid/*INT*/,about/*String*/," +
                            "weight/*Double*/,height/*Double*/," +
                            "birth_date/*Date*/,relationship/*Int*/," +
                            "religion/*Int*/,orientation/*Int*/," +
                            "ethnicity/*Int*/,reference/*Int*/," +
                            "stds/*LONGTEXT*/,role/*Int*/," +
                            "disability/*LONGTEXT*/ " +
                            " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)")){
                statement.setInt(1, userInfo.getUid());
                statement.setString(2, userInfo.getAbout());
                statement.setDouble(3, userInfo.getHeight());
                statement.setDouble(4, userInfo.getWeight());
                statement.setDate(5, userInfo.getBirthDate());
                statement.setInt(6, Relationship.getValOf(userInfo.getRelationship()));
                statement.setInt(7, Religion.getValOf(userInfo.getReligion()));
                statement.setInt(8, Orientation.getValOf(userInfo.getOrientation()));
                statement.setInt(9, Ethnicity.getValOf(userInfo.getEthnicity()));
                statement.setInt(10, Reference.getValOf(userInfo.getReference()));
                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.setPrettyPrinting();
                statement.setString(11, gsonBuilder.create().toJson(userInfo.getStDs()));
                statement.setInt(12, Role.getValOf(userInfo.getRole()));
                statement.setString(13, gsonBuilder.create().toJson(userInfo.getDisabilities()));
                int rowsAffected = statement.executeUpdate();
                System.out.println("rows affected: " + rowsAffected);
                return true;
            }
        }catch (SQLIntegrityConstraintViolationException e){
            System.out.println("here we understand that this key already exists in the table");
//            if(e.getMessage().contains("'PRIMARY'")){
//
//            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get Classes.UserInfo object by UID. This function communicates with the DB.
     * @param uid
     * Classes.User UID int.
     * @return
     * Classes.UserInfo object referred by the UID provided.
     */
    public static UserInfo getUserInfoByUID(int uid){
        UserInfo userInfo = null;
        try (Connection conn = DBConnection.getConn()){
            try (PreparedStatement statement = conn.prepareStatement("SELECT * FROM users_info WHERE uid=?")){
                statement.setInt(1, uid);
                try (ResultSet resultSet = statement.executeQuery()){
                    if (resultSet.next()){
                        userInfo = new UserInfo(
                                resultSet.getInt(1),
                                resultSet.getString(2),
                                resultSet.getInt(3),
                                resultSet.getInt(4),
                                resultSet.getDate(5),
                                Relationship.getEnumValOf(resultSet.getInt(6)),
                                Religion.getEnumValOf(resultSet.getInt(7)),
                                Orientation.getEnumValOf(resultSet.getInt(8)),
                                Ethnicity.getEnumValOf(resultSet.getInt(9)),
                                Reference.getEnumValOf(resultSet.getInt(10)),
                                STD.getEnumsFrom(getGson().fromJson(JsonParser.parseReader(resultSet.getCharacterStream(11)), int[].class)),
                                Role.getEnumValOf(resultSet.getInt(12)),
                                Disability.getEnumsFrom(getGson().fromJson(JsonParser.parseReader(resultSet.getCharacterStream(13)), int[].class)));
                    }
                }catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userInfo;
    }

    /**
     * Updates user info. This functions communicates with the DB.
     * @param uid
     * UID for the user to update their location.
     * @param userinfo
     * Updated Classes.UserInfo object containing.
     */
    public static void updateUserInfo(int uid, UserInfo userinfo) {

        try (Connection conn = getConn()) {
            try (PreparedStatement statement = conn.prepareStatement(
                    "UPDATE users_info " +
                            "SET about=?/*String*/,weight=?/*Double*/," +
                            "height=?/*Double*/,birth_date=?/*Date*/," +
                            "relationship=?/*Int*/,religion=?/*Int*/," +
                            "orientation=?/*Int*/,ethnicity=?/*Int*/," +
                            "reference=?/*Int*/,stds=?/*LONGTEXT*/," +
                            "role=?/*Int*/,disability=?/*LONGTEXT*/ " +
                            "WHERE uid=?")){
                statement.setString(1, userinfo.getAbout());
                statement.setDouble(2, userinfo.getHeight());
                statement.setDouble(3, userinfo.getWeight());
                statement.setDate(4, userinfo.getBirthDate());
                statement.setInt(5, Relationship.getValOf(userinfo.getRelationship()));
                statement.setInt(6, Religion.getValOf(userinfo.getReligion()));
                statement.setInt(7, Orientation.getValOf(userinfo.getOrientation()));
                statement.setInt(8, Ethnicity.getValOf(userinfo.getEthnicity()));
                statement.setInt(9, Reference.getValOf(userinfo.getReference()));
                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.setPrettyPrinting();
                statement.setString(10, gsonBuilder.create().toJson(userinfo.getStDs()));
                statement.setInt(11, Role.getValOf(userinfo.getRole()));
                statement.setString(12, gsonBuilder.create().toJson(userinfo.getDisabilities()));
                statement.setInt(13, uid);
            }catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * Write Json object to OutputStream.
     * @param outputStream
     * OutputStream to write to.
     */
    public void write(OutputStream outputStream) throws IOException {
        System.out.println("UserInfo write: " + this.toString());
        byte[] bytes = this.toString().getBytes();
        outputStream.write(bytes.length);
        outputStream.write(bytes);
    }

//
//    /**
//     * creates a json object to return to Client with Userinfo UID instead of Object.
//     * @return
//     * String: json object of Class User.
//     */
//    public String toClientString() {
//        JsonObject object = new JsonObject();
//        object.addProperty(UID, this.uid);
//        object.addProperty(, this.username);
//        object.addProperty(LAT, this.geoPoint.getLat());
//        object.addProperty(LNG, this.geoPoint.getLng());
//        object.addProperty(IMG_URL, this.img_url);
//        JsonArray favsArr = new JsonArray(favs.size());
//        for (Integer num : favs) {
//            favsArr.add(num);
//        }
//        object.add(FAVS, favsArr);
//        JsonArray roomsArr = new JsonArray(chatRooms.size());
//        for (Room room : chatRooms) {
//            roomsArr.add(room.getRoomUid());
//        }
//        object.add(ROOMS, roomsArr);
//        object.addProperty(INFO, this.info.getUid());
//
//        return object.toString();
//    }

    /**
     * Override toString function to create a json object.
     * @return
     * String: json object of Classes.UserInfo.
     */
    @Override
    public String toString() {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();

        Gson gson = builder.create();
        return gson.toJson(this);
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public Relationship getRelationship() {
        return relationship;
    }

    public void setRelationship(Relationship relationship) {
        this.relationship = relationship;
    }

    public Religion getReligion() {
        return religion;
    }

    public void setReligion(Religion religion) {
        this.religion = religion;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
    }

    public Ethnicity getEthnicity() {
        return ethnicity;
    }

    public void setEthnicity(Ethnicity ethnicity) {
        this.ethnicity = ethnicity;
    }

    public Reference getReference() {
        return reference;
    }

    public void setReference(Reference reference) {
        this.reference = reference;
    }

    public STD[] getStDs() {
        return stDs;
    }

    public void setStDs(STD[] stDs) {
        this.stDs = stDs;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Disability[] getDisabilities() {
        return disabilities;
    }

    public void setDisabilities(Disability[] disabilities) {
        this.disabilities = disabilities;
    }

    public enum Relationship {
        NOT_DEFINED, SINGLE, IN_RELATIONSHIP;

        static public int getValOf(Relationship relationship){
            switch (relationship){
                case SINGLE:
                    return 1;
                case IN_RELATIONSHIP:
                    return 2;
                default:
                    return 0;
            }
        }
        static public Relationship getEnumValOf(int code){
            switch (code){
                case 1:
                    return SINGLE;
                case 2:
                    return IN_RELATIONSHIP;
                default:
                    return NOT_DEFINED;
            }
        }
    }
                                                //TODO
    public enum Religion {
        NOT_DEFINED, CHRISTIAN, MUSLIM, ATHEIST;

        static public int getValOf(Religion enumObj){
            switch (enumObj){
                case CHRISTIAN:
                    return 1;
                case MUSLIM:
                    return 2;
                case ATHEIST:
                    return 3;
                default:
                    return 0;
            }
        }
        static public Religion getEnumValOf(int code){
            switch (code){
                case 1:
                    return CHRISTIAN;
                case 2:
                    return MUSLIM;
                case 3:
                    return ATHEIST;
                default:
                    return NOT_DEFINED;
            }
        }

    }

    public enum Orientation {
        NOT_DEFINED, GAY, BISEXUAL, PANSEXUAL;

        static public int getValOf(Orientation enumObj){
            switch (enumObj){
                case GAY:
                    return 1;
                case BISEXUAL:
                    return 2;
                case PANSEXUAL:
                    return 3;
                default:
                    return 0;
            }
        }
        static public Orientation getEnumValOf(int code){
            switch (code){
                case 1:
                    return GAY;
                case 2:
                    return BISEXUAL;
                case 3:
                    return PANSEXUAL;
                default:
                    return NOT_DEFINED;
            }
        }
    }

    public enum Ethnicity {
        NOT_DEFINED, WHITE, MIDDLE_EASTERN, LATINO, BLACK;

        static public int getValOf(Ethnicity enumObj){
            switch (enumObj){
                case WHITE:
                    return 1;
                case MIDDLE_EASTERN:
                    return 2;
                case LATINO:
                    return 3;
                case BLACK:
                    return 4;
                default:
                    return 0;
            }
        }
        static public Ethnicity getEnumValOf(int code){
            switch (code){
                case 1:
                    return WHITE;
                case 2:
                    return MIDDLE_EASTERN;
                case 3:
                    return LATINO;
                case 4:
                    return BLACK;
                default:
                    return NOT_DEFINED;
            }
        }
    }

    public enum Reference {
        NOT_DEFINED, HE, SHE, THEY;

        static public int getValOf(Reference enumObj){
            switch (enumObj){
                case HE:
                    return 1;
                case SHE:
                    return 2;
                case THEY:
                    return 3;
                default:
                    return 0;
            }
        }
        static public Reference getEnumValOf(int code){
            switch (code){
                case 1:
                    return HE;
                case 2:
                    return SHE;
                case 3:
                    return THEY;
                default:
                    return NOT_DEFINED;
            }
        }
    }

    public enum STD {
        NOT_DEFINED, NO_STDS, HIV, AIDS;

        static public STD[] getEnumsFrom(int[] codes) {
            STD[] stds = new STD[codes.length];
            for (int i = 0; i < codes.length; i++) {
                stds[i] = getEnumValOf(codes[i]);
            }
            return stds;
        }

        static public int getValOf(STD enumObj){
            switch (enumObj){
                case NO_STDS:
                    return 1;
                case HIV:
                    return 2;
                case AIDS:
                    return 3;
                default:
                    return 0;
            }
        }
        static public STD getEnumValOf(int code){
            switch (code){
                case 1:
                    return NO_STDS;
                case 2:
                    return HIV;
                case 3:
                    return AIDS;
                default:
                    return NOT_DEFINED;
            }
        }
    }

    public enum Role {
        NOT_DEFINED, TOP, BOTTOM, VERSATILE, VERSATILE_TOP, VERSATILE_BOTTOM;

        static public int getValOf(Role enumObj){
            switch (enumObj){
                case TOP:
                    return 1;
                case BOTTOM:
                    return 2;
                case VERSATILE:
                    return 3;
                case VERSATILE_TOP:
                    return 4;
                case VERSATILE_BOTTOM:
                    return 5;
                default:
                    return 0;
            }
        }
        static public Role getEnumValOf(int code){
            switch (code){
                case 1:
                    return TOP;
                case 2:
                    return BOTTOM;
                case 3:
                    return VERSATILE;
                case 4:
                    return VERSATILE_TOP;
                case 5:
                    return VERSATILE_BOTTOM;
                default:
                    return NOT_DEFINED;
            }
        }
    }

    public enum Disability {
        NOT_DEFINED, BLIND;

        static public Disability[] getEnumsFrom(int[] codes) {
            Disability[] disabilities = new Disability[codes.length];
            for (int i = 0; i < codes.length; i++) {
                disabilities[i] = getEnumValOf(codes[i]);
            }
            return disabilities;
        }

        static public int getValOf(Disability enumObj){
            switch (enumObj){
                case BLIND:
                    return 1;
                default:
                    return 0;
            }
        }
        static public Disability getEnumValOf(int code){
            switch (code){
                case 1:
                    return BLIND;
                default:
                    return NOT_DEFINED;
            }
        }
    }


}

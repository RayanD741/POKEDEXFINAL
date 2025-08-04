package Classes;

import com.google.gson.annotations.SerializedName;

public class TypeSlot {

    @SerializedName("type")
    private TypeInfo typeInfo;

    public TypeInfo getTypeInfo() {
        return typeInfo;
    }

    public void setTypeInfo(TypeInfo typeInfo) {
        this.typeInfo = typeInfo;
    }

    public static class TypeInfo {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}

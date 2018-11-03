package adapters;

/**
 * Created by elvis.pobric on 26/05/2017.
 */

public class DrawerItem {

    public Integer id;
    public Integer drawable;
    public Integer icon;
    public String text;
    public String text_extra;

    public DrawerItem(Integer id, Integer drawable, Integer icon, String text, String text_extra){
        this.id = id;
        this.drawable = drawable;
        this.icon = icon;
        this.text = text;
        this.text_extra = text_extra;
    }

}

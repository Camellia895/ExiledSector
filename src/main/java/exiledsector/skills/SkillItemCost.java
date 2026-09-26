package exiledsector.skills;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;

public record SkillItemCost(String itemId, float quantity) {

    public String commodityName() {
        CommoditySpecAPI spec = Global.getSettings().getCommoditySpec(itemId);
        return spec != null ? spec.getName() : itemId;
    }

    public String formattedQuantity() {
        return formatQuantity(quantity);
    }

    public static String formatQuantity(float value) {
        return value == Math.rint(value) ? String.valueOf((int) value) : String.valueOf(value);
    }
}

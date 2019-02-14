package muramasa.gregtech.api.items;

import muramasa.gregtech.api.GregTechAPI;
import muramasa.gregtech.api.capability.ICoverHandler;
import muramasa.gregtech.api.capability.ITechCapabilities;
import muramasa.gregtech.api.cover.CoverBehaviour;
import muramasa.gregtech.api.data.Materials;
import muramasa.gregtech.api.enums.Element;
import muramasa.gregtech.api.materials.Material;
import muramasa.gregtech.api.materials.Prefix;
import muramasa.gregtech.api.util.Utils;
import muramasa.gregtech.client.creativetab.GregTechTab;
import muramasa.gregtech.common.utils.Ref;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

public class MaterialItem extends Item {

    private static LinkedHashMap<String, MaterialItem> TYPE_LOOKUP = new LinkedHashMap<>();

    private String material, prefix;

    public MaterialItem(Prefix prefix, Material material) {
        setUnlocalizedName(Ref.MODID + "_item_" + prefix.getName() + "_" + material.getName());
        setRegistryName("item_" + prefix.getName() + "_" + material.getName());
        setCreativeTab(Ref.TAB_MATERIALS);
        this.material = material.getName();
        this.prefix = prefix.getName();
        TYPE_LOOKUP.put(prefix.getName() + material.getName(), this);
    }

    public static void init() {
        for (Prefix prefix : Prefix.getAll()) {
            for (Material material : Materials.getAll()) {
                if (!prefix.allowGeneration(material)) continue;
                new MaterialItem(prefix, material);
            }
        }
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (tab instanceof GregTechTab) {
            if (((GregTechTab) tab).getTabName().equals("materials")) {
                if (getPrefix().isVisible()) {
                    items.add(new ItemStack(this));
                }
            }
        }
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        MaterialItem item = (MaterialItem) stack.getItem();
        return item.getPrefix().getDisplayName(item.getMaterial());
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        Element element = ((MaterialItem) stack.getItem()).getMaterial().getElement();
        if (element != null) {
            tooltip.add(element.getDisplayName());
        }
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        TileEntity tile = Utils.getTile(world, pos);
        if (tile != null) {
            if (tile.hasCapability(ITechCapabilities.COVERABLE, null)) {
                ICoverHandler coverHandler = tile.getCapability(ITechCapabilities.COVERABLE, facing);
                if (coverHandler == null) return EnumActionResult.FAIL;
                CoverBehaviour behaviour = GregTechAPI.getCoverBehaviour(stack);
                if (behaviour == null) return EnumActionResult.FAIL;
                EnumFacing targetSide = Utils.determineInteractionSide(facing, hitX, hitY, hitZ);
                if (behaviour.needsNewInstance()) {
                    behaviour = behaviour.getNewInstance(stack);
                }
                if (coverHandler.setCover(targetSide, behaviour)) {
                    stack.shrink(1);
                }
                return EnumActionResult.SUCCESS;
            }
        }
        return EnumActionResult.FAIL;
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        String set = getMaterial().getSet().getName();
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(Ref.MODID + ":material_set_item/" + set, set + "=" + prefix));
    }

    public Prefix getPrefix() {
        return Prefix.get(prefix);
    }

    public Material getMaterial() {
        return Materials.get(material);
    }

    public static boolean hasPrefix(ItemStack stack, Prefix prefix) {
        return stack.getItem() instanceof MaterialItem && ((MaterialItem) stack.getItem()).getPrefix() == prefix;
    }

    public static boolean hasMaterial(ItemStack stack, Material material) {
        return stack.getItem() instanceof MaterialItem && ((MaterialItem) stack.getItem()).getMaterial() == material;
    }

    public static boolean doesShowExtendedHighlight(ItemStack stack) {
        return hasPrefix(stack, Prefix.Plate);
    }

    public static ItemStack get(Prefix prefix, Material material, int count) {
        return new ItemStack(TYPE_LOOKUP.get(prefix.getName() + material.getName()), count);
    }

    public static Collection<MaterialItem> getAll() {
        return TYPE_LOOKUP.values();
    }

    public static class ColorHandler implements IItemColor {
        @Override
        public int colorMultiplier(ItemStack stack, int tintIndex) {
            if (tintIndex == 0) {
                if (stack.getItem() instanceof MaterialItem) {
                    Material material = ((MaterialItem) stack.getItem()).getMaterial();
                    if (material != null) {
                        return material.getRGB();
                    }
                }
            }
            return -1;
        }
    }
}

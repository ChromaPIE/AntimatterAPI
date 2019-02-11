package muramasa.gregtech.common.blocks;

import muramasa.gregtech.api.data.Materials;
import muramasa.gregtech.api.properties.ITechProperties;
import muramasa.gregtech.common.utils.Ref;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.LinkedHashMap;

public class BlockOre extends Block {

    private static LinkedHashMap<String, BlockOre> blockLookup = new LinkedHashMap<>();

    private String type;

    public BlockOre(String type) {
        super(net.minecraft.block.material.Material.ROCK);
        setUnlocalizedName(Ref.MODID + "_ore_" + type);
        setRegistryName("ore_" + type);
        setCreativeTab(Ref.TAB_BLOCKS);
        this.type = type;
        blockLookup.put(type, this);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer.Builder(this).add(ITechProperties.MATERIAL).add(ITechProperties.STONE).build();
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        IExtendedBlockState exState = (IExtendedBlockState) state;
        return exState.withProperty(ITechProperties.MATERIAL, Materials.get(type).getId());
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(ITechProperties.STONE);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(ITechProperties.STONE, meta);
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
        items.add(new ItemStack(this, 1, Materials.get(type).getId()));
    }

    @Override
    public float getBlockHardness(IBlockState blockState, World worldIn, BlockPos pos) {
        return 1.0f + (getHarvestLevel(blockState) * 1.0f);
    }

    @Override
    public int getHarvestLevel(IBlockState state) {
        return 1;
    }

    //TODO used for testing only
    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        world.setBlockState(pos, state.withProperty(ITechProperties.STONE, RANDOM.nextInt(6)));
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return new ItemStack(this, 1, Materials.get(type).getId());
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        drops.add(Materials.get(type).getChunk(1));
    }

    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), Materials.get(type).getId(), new ModelResourceLocation(getRegistryName(), "inventory"));
    }

    public String getType() {
        return type;
    }

    public static BlockOre get(String type) {
        return blockLookup.get(type);
    }

    public static Collection<BlockOre> getAll() {
        return blockLookup.values();
    }

    public static class ColorHandler implements IBlockColor {
        @Override
        public int colorMultiplier(IBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos, int tintIndex) {
            if (tintIndex == 1) {
                BlockOre block = (BlockOre) state.getBlock();
                return Materials.get(block.getType()).getRGB();
            }
            return -1;
        }
    }
}

package muramasa.gtu.api.ore;

import muramasa.gtu.Ref;
import muramasa.gtu.api.GregTechAPI;
import muramasa.gtu.api.materials.Material;
import muramasa.gtu.api.materials.MaterialType;
import muramasa.gtu.api.registration.IColorHandler;
import muramasa.gtu.api.registration.IGregTechObject;
import muramasa.gtu.api.registration.IItemBlock;
import muramasa.gtu.api.registration.IModelOverride;
import muramasa.gtu.api.tileentities.TileEntityOre;
import muramasa.gtu.api.util.Utils;
import muramasa.gtu.client.render.StateMapperRedirect;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

import static muramasa.gtu.api.properties.GTProperties.*;

public class BlockOre extends Block implements IGregTechObject, IItemBlock, IModelOverride, IColorHandler {

    public BlockOre() {
        super(net.minecraft.block.material.Material.ROCK);
        setUnlocalizedName(getId());
        setRegistryName(getId());
        setCreativeTab(Ref.TAB_BLOCKS);
        GregTechAPI.register(BlockOre.class, this);
    }

    @Override
    public String getId() {
        return "block_ore";
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer.Builder(this).add(ORE_MATERIAL, ORE_STONE, ORE_TYPE).build();
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        IExtendedBlockState exState = (IExtendedBlockState) state;
        TileEntity tile = Utils.getTile(world, pos);
        if (tile instanceof TileEntityOre) {
            TileEntityOre ore = (TileEntityOre) tile;
            exState = exState
                .withProperty(ORE_MATERIAL, ore.getMaterial().getInternalId())
                .withProperty(ORE_STONE, ore.getStoneType().getInternalId())
                .withProperty(ORE_TYPE, ore.getType().getInternalId());
        }
        return exState;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState();
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return 0;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityOre();
    }

    @Override
    public SoundType getSoundType(IBlockState state, World world, BlockPos pos, @Nullable Entity entity) {
        TileEntity tile = Utils.getTile(world, pos);
        if (tile instanceof TileEntityOre) {
            return ((TileEntityOre) tile).getStoneType().getSoundType();
        }
        return SoundType.STONE;
    }

    //TODO
    @Override
    public float getBlockHardness(IBlockState blockState, World worldIn, BlockPos pos) {
        return 1.0f + (getHarvestLevel(blockState) * 1.0f);
    }

    //TODO
    @Override
    public int getHarvestLevel(IBlockState state) {
        return 1;
    }

    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        return Ref.ORE_VEIN_SPECTATOR_DEBUG || super.shouldSideBeRendered(state, world, pos, side);
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        return Ref.ORE_VEIN_SPECTATOR_DEBUG ? 15 : 0;
    }

    @Override
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
        for (MaterialType type : MaterialType.ORE_TYPES.values()) {
            //if (!type.isVisible()) continue;
            StoneType.getAll().forEach(s -> type.getMats().forEach(m -> {
                items.add(new OreStack(m, s, type).asItemStack());
            }));
        }
    }

    /** TileEntity Drops Start **/
    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        TileEntity tile = Utils.getTile(world, pos);
        if (tile instanceof TileEntityOre) {
            TileEntityOre ore = (TileEntityOre) tile;
            drops.add(new OreStack(ore.getMaterial(), ore.getStoneType(), ore.getType()).asItemStack());
        }
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        if (willHarvest) return true;
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }

    @Override
    public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity tile, ItemStack stack) {
        super.harvestBlock(world, player, pos, state, tile, stack);
        world.setBlockToAir(pos);
    }
    /** TileEntity Drops End **/

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey(Ref.KEY_ORE_STACK_STONE)) {
            TileEntity tile = Utils.getTile(world, pos);
            if (tile instanceof TileEntityOre) {
                Material material = Material.get(stack.getTagCompound().getInteger(Ref.KEY_ORE_STACK_MATERIAL));
                StoneType stoneType = StoneType.get(stack.getTagCompound().getInteger(Ref.KEY_ORE_STACK_STONE));
                MaterialType materialType = MaterialType.ORE_TYPES.get(stack.getTagCompound().getInteger(Ref.KEY_ORE_STACK_TYPE));
                ((TileEntityOre) tile).init(material, stoneType, materialType);
            }
        }
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        TileEntity tile = Utils.getTile(world, pos);
        if (tile instanceof TileEntityOre) {
            TileEntityOre ore = (TileEntityOre) tile;
            return new OreStack(ore.getMaterial(), ore.getStoneType(), ore.getType()).asItemStack();
        }
        return ItemStack.EMPTY;
    }

    @Override
    public String getDisplayName(ItemStack stack) {
        if (!stack.hasTagCompound()) return stack.getUnlocalizedName();
        if (stack.getTagCompound().hasKey(Ref.KEY_ORE_STACK_STONE)) {
            Material material = Material.get(stack.getTagCompound().getInteger(Ref.KEY_ORE_STACK_MATERIAL));
            StoneType stoneType = StoneType.get(stack.getTagCompound().getInteger(Ref.KEY_ORE_STACK_STONE));
            MaterialType materialType = MaterialType.ORE_TYPES.get(stack.getTagCompound().getInteger(Ref.KEY_ORE_STACK_TYPE));
            return stoneType.getId() + "." + material.getId() + "." + materialType.getId() + ".name";
        }
        return stack.getUnlocalizedName();
    }

    @Override
    public int getBlockColor(IBlockState state, @Nullable IBlockAccess world, @Nullable BlockPos pos, int i) {
        TileEntity tile = Utils.getTile(world, pos);
        return tile instanceof TileEntityOre && i == 1 ? ((TileEntityOre) tile).getMaterial().getRGB() : -1;
    }

    @Override
    public int getItemColor(ItemStack stack, @Nullable Block block, int i) {
        if (!stack.hasTagCompound()) return -1;
        return i == 1 ? Material.get(stack.getTagCompound().getInteger(Ref.KEY_ORE_STACK_MATERIAL)).getRGB() : -1;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onModelRegistration() {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(Ref.MODID + ":block_ore", "inventory"));
        ModelLoader.setCustomStateMapper(this, new StateMapperRedirect(new ResourceLocation(Ref.MODID, "block_ore")));
    }
}

package com.nobtg.Mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * This class is part of the ji_GGO project.
 * It contains code from the GPL-licensed component.
 * <p>
 * Original author: ji_GGO
 * <p>
 * This code is distributed under the terms of the GNU General Public License.
 * You should have received a copy of the GNU General Public License along with this code.
 * If not, see <a href="https://www.gnu.org/licenses/gpl-3.0.html">https://www.gnu.org/licenses/gpl-3.0.html</a>.
 * <p>
 * <p>
 * 此类是 ji_GGO 项目的一部分。
 * 它包含了使用 GPL 许可证的组件的代码。
 * <p>
 * 原作者：ji_GGO
 * <p>
 * 本代码根据 GNU General Public License 的条款分发。
 * 您应该已经收到了 GNU General Public License 的副本，与此代码一起。
 * 如果没有，访问 <a href="https://www.gnu.org/licenses/gpl-3.0.html">https://www.gnu.org/licenses/gpl-3.0.html</a>。
 */

public class Mixins_Sword_Block {
    @Mixin(UseAnim.class)
    @Unique
    public static class MixinUseAnim {

        @Shadow
        @Final
        @Mutable
        private static UseAnim[] $VALUES;

        @Unique
        private static final UseAnim nOBTG$SWORD = useAnim$addAnim();

        @Invoker("<init>")
        public static UseAnim useAnim$invokeInit(String name, int id) {
            throw new AssertionError();
        }

        @Unique
        private static UseAnim useAnim$addAnim() {
            ArrayList<UseAnim> animations;
            UseAnim animation = null;
            if (MixinUseAnim.$VALUES != null) {
                animations = new ArrayList<>(Arrays.asList(MixinUseAnim.$VALUES));
                animation = useAnim$invokeInit("SWORD:BLOCK", animations.get(animations.size() - 1).ordinal() + 1);
                animations.add(animation);
                MixinUseAnim.$VALUES = animations.toArray(new UseAnim[0]);
            }
            return animation;
        }

    }

    @Mixin(PlayerRenderer.class)
    public abstract static class MixinPlayerRenderer extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

        public MixinPlayerRenderer(EntityRendererProvider.Context context, PlayerModel<AbstractClientPlayer> model, float shadowSize) {
            super(context, model, shadowSize);
        }

        @Inject(method = "getArmPose",
                at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/item/ItemStack;getUseAnimation()Lnet/minecraft/world/item/UseAnim;"),
                locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
        private static void renderSwordMain(AbstractClientPlayer player, InteractionHand hand, CallbackInfoReturnable<HumanoidModel.ArmPose> info,
                                            ItemStack itemstack, UseAnim useanim){
            if (useanim == UseAnim.valueOf("SWORD:BLOCK") && useanim == itemstack.getUseAnimation()) {
                info.setReturnValue(HumanoidModel.ArmPose.BLOCK);
            }
        }

    }

    @Mixin(ItemInHandRenderer.class)
    public abstract static class MixinItemInHandRenderer {

        @Inject(method = "renderArmWithItem",
                at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/item/ItemStack;getUseAnimation()Lnet/minecraft/world/item/UseAnim;"))
        private void renderSwordBlock(AbstractClientPlayer player, float partialTicks, float pitch, InteractionHand hand, float swingProgress, ItemStack stack, float equippedProgress, PoseStack matrices, MultiBufferSource buffer, int combinedLight, CallbackInfo info) {
            if (stack.getUseAnimation() == UseAnim.valueOf("SWORD:BLOCK")) {
                boolean flag = (hand == InteractionHand.MAIN_HAND);
                HumanoidArm arm = flag ? player.getMainArm() : player.getMainArm().getOpposite();
                this.applyItemArmTransform(matrices, arm, equippedProgress);
                // https://github.com/Fuzss/swordblockingmechanics/blob/3280e9cb604c58b8538efb3466cf462fd89d2fc3/src/main/java/com/fuzs/swordblockingmechanics/client/element/SwordBlockingExtension.java#L143
                int horizontal = arm == HumanoidArm.RIGHT ? 1 : -1;
                matrices.translate(horizontal * -0.14142136F, 0.08F, 0.14142136F);
                matrices.mulPose(Axis.XP.rotationDegrees(-102.25F));
                matrices.mulPose(Axis.YP.rotationDegrees(horizontal * 13.365F));
                matrices.mulPose(Axis.ZP.rotationDegrees(horizontal * 78.05F));
            }
        }

        @Shadow
        protected abstract void applyItemArmTransform(PoseStack matrices, HumanoidArm arm, float equippedProgress);

    }

    @Mixin(HumanoidModel.class)
    public abstract static class MixinHumanoidModel<T extends LivingEntity> extends AgeableListModel<T> implements ArmedModel, HeadedModel {

        @Final
        @Shadow
        public ModelPart rightArm;

        @Final
        @Shadow
        public ModelPart leftArm;

        @Shadow
        public HumanoidModel.ArmPose rightArmPose;

        @Shadow
        public HumanoidModel.ArmPose leftArmPose;

        @Inject(method = "poseRightArm", at = @At(value = "HEAD"), cancellable = true)
        private void renderRight(T entity, CallbackInfo info){
            if (this.rightArmPose == HumanoidModel.ArmPose.BLOCK) {
                nOBTG$renderArm(entity.getMainArm(), this.rightArm);
                info.cancel();
            }
        }

        @Inject(method = "poseLeftArm", at = @At(value = "HEAD"), cancellable = true)
        private void renderLeft(T entity, CallbackInfo info){
            if (this.leftArmPose == HumanoidModel.ArmPose.BLOCK) {
                nOBTG$renderArm(entity.getMainArm(), this.leftArm);
                info.cancel();
            }
        }

        @Inject(method = "setupAttackAnimation", at = @At(value = "HEAD"), cancellable = true)
        private void renderCancel(T entity, float ageInTicks, CallbackInfo info){
            if (entity.getMainArm() == HumanoidArm.RIGHT && this.rightArmPose == HumanoidModel.ArmPose.BLOCK) {
                info.cancel();
            } else if (entity.getMainArm() == HumanoidArm.LEFT && this.leftArmPose == HumanoidModel.ArmPose.BLOCK) {
                info.cancel();
            }
        }


        @Unique
        private static void nOBTG$renderArm(HumanoidArm type, ModelPart arm) {
            arm.xRot = arm.xRot * 0.5F - 0.9424778F;
            arm.yRot = (type == HumanoidArm.RIGHT ? 1 : -1) * -0.5235988F;
        }
    }
}

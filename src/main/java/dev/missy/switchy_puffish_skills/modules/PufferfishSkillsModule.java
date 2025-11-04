package dev.missy.switchy_puffish_skills.modules;

import folk.sisby.switchy.api.SwitchyEvents;
import folk.sisby.switchy.api.module.SwitchyModule;
import folk.sisby.switchy.api.module.SwitchyModuleEditable;
import folk.sisby.switchy.api.module.SwitchyModuleInfo;
import folk.sisby.switchy.api.module.SwitchyModuleRegistry;
import folk.sisby.switchy.api.module.SwitchyModuleTransferable;
import folk.sisby.switchy.util.Feedback;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.puffish.skillsmod.api.Skill;
import net.puffish.skillsmod.api.SkillsAPI;
import org.jetbrains.annotations.Nullable;

public class PufferfishSkillsModule extends PufferfishSkillsModuleData
        implements SwitchyModule, SwitchyModuleTransferable, SwitchyEvents.Init {

    private static final String KEY_POINTS_PREFIX = "points_";
    private static final String KEY_XP_TOTAL = "xp_total";

    // save stuff (categories, skills, points, xp)
    @Override
    public void updateFromPlayer(ServerPlayerEntity player, @Nullable String nextPreset) {
        this.skillsData = new NbtCompound();

        SkillsAPI.streamCategories().forEach(category -> {
            NbtCompound categoryData = new NbtCompound();

            // category state (locked/unlocked)
            boolean categoryUnlocked = category.isUnlocked(player);
            categoryData.putBoolean("category_unlocked", categoryUnlocked);

            // skills
            category.streamSkills().forEach(skill -> {
                boolean unlocked = skill.getState(player) == Skill.State.UNLOCKED;
                categoryData.putBoolean(skill.getId(), unlocked);
            });

            // points
            category.streamPointsSources(player).forEach(sourceId -> {
                int points = category.getPoints(player, sourceId);
                categoryData.putInt(KEY_POINTS_PREFIX + sourceId.toString(), points);
            });

            // xp
            category.getExperience().ifPresent(experience ->
                categoryData.putInt(KEY_XP_TOTAL, experience.getTotal(player))
            );

            this.skillsData.put(category.getId().toString(), categoryData);
        });
    }

    // restore/apply skills, points, xp blabla
    @Override
    public void applyToPlayer(ServerPlayerEntity player) {
        SkillsAPI.streamCategories().forEach(category -> {
            NbtCompound categoryData = this.skillsData.getCompound(category.getId().toString());

            // category state (locked/unlocked)
            if (categoryData.contains("category_unlocked")) {
                boolean shouldBeUnlocked = categoryData.getBoolean("category_unlocked");
                if (shouldBeUnlocked && !category.isUnlocked(player)) category.unlock(player);
                else if (!shouldBeUnlocked && category.isUnlocked(player)) category.lock(player);
            }

            // skills
            category.streamSkills().forEach(skill -> {
                boolean unlocked = categoryData.getBoolean(skill.getId());
                Skill.State state = skill.getState(player);

                if (unlocked && state != Skill.State.UNLOCKED) {
                    skill.unlock(player);
                } else if (!unlocked && state == Skill.State.UNLOCKED) {
                    skill.lock(player);
                }
            });

            // points
            category.streamPointsSources(player).forEach(sourceId -> {
                String key = KEY_POINTS_PREFIX + sourceId.toString();
                int points = categoryData.contains(key) ? categoryData.getInt(key) : 0;
                category.setPointsSilently(player, sourceId, points);
            });

            // xp
            category.getExperience().ifPresent(experience -> {
                int xp = categoryData.contains(KEY_XP_TOTAL) ? categoryData.getInt(KEY_XP_TOTAL) : 0;
                experience.setTotal(player, xp);
            });

        });
    }

    @Override
    public void onInitialize() {
        SwitchyModuleRegistry.registerModule(ID, PufferfishSkillsModule::new, new SwitchyModuleInfo(
                false,
                SwitchyModuleEditable.OPERATOR,
                Feedback.translatable("switchy.modules.puffish.skills.desc"))
                .withDescriptionWhenEnabled(Feedback.translatable("switchy.modules.puffish.skills.enable"))
                .withDescriptionWhenDisabled(Feedback.translatable("switchy.modules.puffish.skills.disable"))
                .withDeletionWarning(Feedback.translatable("switchy.modules.puffish.skills.warning"))
        );
    }
}

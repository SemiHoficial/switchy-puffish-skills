package dev.missy.switchy_puffish_skills;

import dev.missy.switchy_puffish_skills.modules.PufferfishSkillsModule;
import net.fabricmc.api.ModInitializer;

public class SwitchyPuffishSkills implements ModInitializer {

    @Override
    public void onInitialize() {
        new PufferfishSkillsModule().onInitialize();
    }
}

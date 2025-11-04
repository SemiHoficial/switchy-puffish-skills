package dev.missy.switchy_puffish_skills.modules;

import folk.sisby.switchy.api.SwitchySerializable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

public class PufferfishSkillsModuleData implements SwitchySerializable {
    public static final String KEY_SKILLS_DATA = "skills_data";
    protected static final Identifier ID = Identifier.of("switchy_puffish_skills", "pufferfish_skills");
    protected NbtCompound skillsData = new NbtCompound();

    @Override
    public NbtCompound toNbt() {
        NbtCompound outNbt = new NbtCompound();
        outNbt.put(KEY_SKILLS_DATA, skillsData);
        return outNbt;
    }

    @Override
    public void fillFromNbt(NbtCompound nbt) {
        this.skillsData = nbt.getCompound(KEY_SKILLS_DATA);
    }
}

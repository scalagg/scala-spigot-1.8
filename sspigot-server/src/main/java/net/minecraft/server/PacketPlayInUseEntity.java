
package net.minecraft.server;

import java.io.IOException;

public class PacketPlayInUseEntity implements Packet<PacketListenerPlayIn> {
	private int a;
	private net.minecraft.server.PacketPlayInUseEntity.EnumEntityUseAction action;
	private Vec3D c;

	public PacketPlayInUseEntity() {
	}

	public void a(PacketDataSerializer var1) throws IOException {
		this.a = var1.e();
		this.action = (net.minecraft.server.PacketPlayInUseEntity.EnumEntityUseAction)var1.a(net.minecraft.server.PacketPlayInUseEntity.EnumEntityUseAction.class);
		if(this.action == net.minecraft.server.PacketPlayInUseEntity.EnumEntityUseAction.INTERACT_AT) {
			this.c = new Vec3D((double)var1.readFloat(), (double)var1.readFloat(), (double)var1.readFloat());
		}

	}

	public void b(PacketDataSerializer var1) throws IOException {
		var1.b(this.a);
		var1.a(this.action);
		if(this.action == net.minecraft.server.PacketPlayInUseEntity.EnumEntityUseAction.INTERACT_AT) {
			var1.writeFloat((float)this.c.a);
			var1.writeFloat((float)this.c.b);
			var1.writeFloat((float)this.c.c);
		}

	}

	public void a(PacketListenerPlayIn var1) {
		var1.a(this);
	}

	public Entity a(World var1) {
		return var1.a(this.a);
	}

	public net.minecraft.server.PacketPlayInUseEntity.EnumEntityUseAction a() {
		return this.action;
	}

	public Vec3D b() {
		return this.c;
	}

	public int getA() {
		return this.a;
	}

	public static enum EnumEntityUseAction {
		INTERACT,
		ATTACK,
		INTERACT_AT;

		private EnumEntityUseAction() {
		}
	}
}

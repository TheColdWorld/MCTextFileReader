package cn.thecoldworld.textfilereader.mixin;

import cn.thecoldworld.textfilereader.mod;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class Server {

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo info) {

        if(mod.TickEvent.isEmpty())
        {
            if(((MinecraftServer) (Object) this).getTicks()%10 == 0)
            {
                //Will Update Permission here
                //mod.GlobalPermission.Update();
                //mod.WorldPermission.Update();
            }
            return;
        }
        try {
            mod.TickEvent.take().run();
        } catch (InterruptedException e) {
            mod.Log.error("",e);
        }
    }
}

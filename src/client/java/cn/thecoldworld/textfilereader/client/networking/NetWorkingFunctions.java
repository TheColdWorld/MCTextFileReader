package cn.thecoldworld.textfilereader.client.networking;

import cn.thecoldworld.textfilereader.api.funcitons;
import cn.thecoldworld.textfilereader.client.api.event.Events;
import cn.thecoldworld.textfilereader.client.screen.MainGUI;
import cn.thecoldworld.textfilereader.client.screen.ServerSettingsGUI;
import cn.thecoldworld.textfilereader.networking.ResponseNetworkPackage;
import cn.thecoldworld.textfilereader.networking.SendNetworkPackage;
import cn.thecoldworld.textfilereader.networking.jsonformats.CommonMessage;
import cn.thecoldworld.textfilereader.networking.jsonformats.S2COpenSettingGui;
import cn.thecoldworld.textfilereader.networking.jsonformats.ToastMessage;
import cn.thecoldworld.textfilereader.variables;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.nio.charset.StandardCharsets;

public abstract class NetWorkingFunctions {
    @Environment(EnvType.CLIENT)
    public static void OnReceiveSedPackage(MinecraftClient client, Identifier identifier, SendNetworkPackage sendNetworkPackage) {
        if (identifier.equals(variables.Identifiers.ControlingIdentifier)) {
            try {
                if (S2COpenSettingGui.IsInstance(sendNetworkPackage.Body.toString())) {
                    client.execute(() -> client.setScreen(new ServerSettingsGUI(client.currentScreen, client).setOptions(variables.defaultGson.fromJson(
                            sendNetworkPackage.Body.toString(), S2COpenSettingGui.class
                    ).Setting)));
                    return;
                }
                if (ToastMessage.IsInstance(sendNetworkPackage.Body)) {
                    ToastMessage toastMessage = ToastMessage.GetInstance(sendNetworkPackage.Body.toString());
                    client.execute(() -> client.getToastManager().add(new SystemToast(SystemToast.Type.PERIODIC_NOTIFICATION, Text.literal(toastMessage.Title),
                            toastMessage.GetMCText())));
                    return;
                }
                if (CommonMessage.IsInstance(sendNetworkPackage.Body)) {
                    CommonMessage message = CommonMessage.GetInstance(sendNetworkPackage.Body.toString());
                    if (message.message.equals("Gui_Open::MainGUI")) {
                        client.execute(() -> client.setScreen(new MainGUI(client.currentScreen, client)));
                        return;
                    }
                }
            } catch (Exception e) {
                variables.Log.error("", e);
            }
        }
    }

    @Environment(EnvType.CLIENT)
    public static void GetNetPackageCallback(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender, Identifier Identifier) {
        if (ResponseNetworkPackage.IsResponse(buf, StandardCharsets.UTF_8)) {
            ResponseNetworkPackage responseNetworkPackage = ResponseNetworkPackage.GetPackage(buf, StandardCharsets.UTF_8);
            if (responseNetworkPackage == null || responseNetworkPackage.ResponseID == null)
                return;
            funcitons.AutoRemoveGetItemFromStream(ClientNetWorkingTask.TaskPool,
                            t -> !t.isReturned() && t.PackageID.equals(responseNetworkPackage.ResponseID))
                    .forEach(i -> i.Return(new S2CArguments(client, responseNetworkPackage.Body)));
        } else if (SendNetworkPackage.IsSendPackage(buf, StandardCharsets.UTF_8)) {
            Events.S2CSendPacketEvent.EVENT.Invoke(client, handler, SendNetworkPackage.GetPackage(buf, StandardCharsets.UTF_8), responseSender, Identifier);
        } else {
            Events.S2CPacketEvent.EVENT.Invoke(client, handler, buf, responseSender, Identifier);
        }
    }
}

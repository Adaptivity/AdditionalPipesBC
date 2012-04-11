package net.minecraft.src.buildcraft.additionalpipes;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import net.minecraft.src.NetworkManager;
import net.minecraft.src.Packet1Login;
import net.minecraft.src.forge.IConnectionHandler;
import net.minecraft.src.forge.IPacketHandler;
import net.minecraft.src.forge.MessageManager;

public class NetworkHandler implements IConnectionHandler, IPacketHandler {

    public static final String CHANNEL = "AdditionalPipes";
    
    @Override
    public void onConnect(NetworkManager network) {
    }

    @Override
    public void onLogin(NetworkManager network, Packet1Login login) {
        
        MessageManager messageManager = MessageManager.getInstance();
        messageManager.registerChannel(network, this, CHANNEL);
        messageManager.addActiveChannel(network, CHANNEL);
    }

    @Override
    public void onDisconnect(NetworkManager network, String message, Object[] args) {
        
        MessageManager messageManager = MessageManager.getInstance();
        messageManager.unregisterChannel(network, this, CHANNEL);
        messageManager.removeActiveChannel(network, CHANNEL);
    }

    @Override
    public void onPacketData(NetworkManager network, String channel, byte[] rawData) {
        
        DataInputStream data = new DataInputStream(new ByteArrayInputStream(rawData));
        
        
        try {
            
            int packetID = data.read();
            switch(packetID) {
                
            }
            
        } catch (IOException e) {
            
        }
    }

}

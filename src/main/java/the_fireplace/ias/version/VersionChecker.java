package the_fireplace.ias.version;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.ClickEvent.Action;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import net.minecraftforge.fml.relauncher.FMLInjectionData;
import the_fireplace.ias.tools.Reference;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.Calendar;
import java.util.Set;

/**
 * This is the embeddable version checker I made. Designed for convenience, all you have to do is paste this class, change the HostMORNAME, HostMODID, HostVERSION, and how it retrieves the latest version and link to the download page.
 * Now uses Curse to find out the latest version. File name must be "HostMODNAME w.x.y.z.jar" or it will probably crash.
 *  @author The_Fireplace
 */
@Mod(modid=VersionChecker.MODID, name=VersionChecker.MODNAME, version=VersionChecker.VERSION, guiFactory = "the_fireplace.ias.version.VersionChecker")
public class VersionChecker implements IModGuiFactory {
	private static final String HostMODID= Reference.MODID;
	private static final String HostMODNAME=Reference.MODNAME;
	private static final String HostVERSION=Reference.VERSION;
	static final String MODID=HostMODID+"vc";
	static final String MODNAME=HostMODNAME+" Version Checker";
	static final String VERSION="1.2";
	private final String curseCode;
	private String latest="0.0.0.0";

	private static Configuration config;
	private static Property FREQUENCY_PROPERTY;
	private static Property LASTCHECKED_PROPERTY;
	private static String freq;
	private static String lc;

	public VersionChecker(){
		curseCode = Reference.curseCode;
	}

	private void syncConfig(){
		freq = FREQUENCY_PROPERTY.getString();
		lc = LASTCHECKED_PROPERTY.getString();
		if(config.hasChanged())
			config.save();
	}

	private boolean canNotify(){
		if(freq.equals("Always"))
			return true;
		int[] date = new int[3];
		date[0] = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
		date[1] = Calendar.getInstance().get(Calendar.MONTH);
		date[2] = Calendar.getInstance().get(Calendar.YEAR);
		if(freq.equals("Daily") && isNotToday(date))
			return true;
		if(freq.equals("Weekly")){
			for(int i=2;i>=0;i--){
				if(i != 0 && date[i] > splitVersion(lc)[i])
					return true;
				if(i == 0 && date[i]-7 > splitVersion(lc)[i])
					return true;
			}
		}
		return false;
	}

	private void tryNotifyClient(EntityPlayer player){
		if(!Loader.isModLoaded("VersionChecker") && isHigherVersion() && canNotify()){
			player.addChatMessage(new ChatComponentText("A new version of "+HostMODNAME+" is available!"));
			player.addChatMessage(new ChatComponentText("=========="+latest+"=========="));
			player.addChatMessage(new ChatComponentText("Get it at the following link:"));
			player.addChatMessage(new ChatComponentText(getDownloadUrl()).setChatStyle(new ChatStyle().setItalic(true).setUnderlined(true).setColor(EnumChatFormatting.BLUE).setChatClickEvent(new ClickEvent(Action.OPEN_URL, getDownloadUrl()))));
			setChecked();
		}
	}

	private void notifyServer(){
		System.out.println("Version "+latest+" of "+HostMODNAME+" is available!");
		System.out.println("Download it at "+getDownloadUrl());
		setChecked();
	}

	private void setChecked(){
		LASTCHECKED_PROPERTY.set(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)+"."+Calendar.getInstance().get(Calendar.MONTH)+"."+Calendar.getInstance().get(Calendar.YEAR));
		syncConfig();
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event){
		config = new Configuration(new File(event.getModConfigurationDirectory(), "fireplace_update_checker.cfg"));
		config.load();
		FREQUENCY_PROPERTY = config.get(Configuration.CATEGORY_GENERAL, "Frequency", "Always");
		FREQUENCY_PROPERTY.setValidValues(new String[]{"Always","Daily","Weekly"});
		LASTCHECKED_PROPERTY = config.get("hidden", "Last Checked", "00.00.0000");
		syncConfig();
		cacheJson();
		latest=getVersionFromJson();
		event.getModMetadata().autogenerated=false;
		event.getModMetadata().modId=MODID;
		event.getModMetadata().name=MODNAME;
		event.getModMetadata().description="The version checker for "+HostMODNAME;
		event.getModMetadata().version=VERSION;
		event.getModMetadata().authorList.add("The_Fireplace");
	}
	@EventHandler
	public void init(FMLInitializationEvent event){
		tryNotifyDynious();
		FMLCommonHandler.instance().bus().register(this);
	}

	@EventHandler
	public void serverStarted(FMLServerStartedEvent event){
		if(event.getSide().isServer())
			tryNotifyServer();
	}

	@SubscribeEvent
	public void onPlayerJoinClient(final ClientConnectedToServerEvent event){
		(new Thread(){
			@Override
			public void run(){
				while(FMLClientHandler.instance().getClientPlayerEntity() == null)
					try{
						Thread.sleep(100);
					}catch(InterruptedException e){

					}
				tryNotifyClient(FMLClientHandler.instance().getClientPlayerEntity());
			}
		}).start();
	}

	@SubscribeEvent
	public void configChanged(ConfigChangedEvent.OnConfigChangedEvent event){
		if(event.modID == MODID)
			syncConfig();
	}

	private String getDownloadUrl(){
		return "http://www.curse.com/mc-mods/minecraft/"+curseCode;
	}

	private boolean isHigherVersion(){
		final int[] _current = splitVersion(HostVERSION);
		final int[] _new = splitVersion(latest);

		for(int i=0;i<Math.max(_current.length, _new.length);i++){
			int curv=0;
			if(i < _current.length)
				curv = _current[i];
			int newv=0;
			if(i<_new.length)
				newv=_new[i];
			if(newv>curv)
				return true;
			else if(curv>newv)
				return false;
		}
		return false;
	}

	private boolean isNotToday(int[] date){
		final int[] _current = splitVersion(lc);

		for(int i = 0; i<Math.max(_current.length, date.length); i++){
			int curv=0;
			if(i < _current.length)
				curv = _current[i];
			int newv=0;
			if(i< date.length)
				newv= date[i];
			if(newv>curv)
				return true;
			else if(curv>newv)
				return false;
		}
		return false;
	}

	private int[] splitVersion(String version){
		final String[] tmp = version.split("\\.");
		final int size=tmp.length;
		final int[] out = new int[size];
		for(int i=0;i<size;i++){
			out[i]=Integer.parseInt(tmp[i]);
		}
		return out;
	}

	private void tryNotifyServer(){
		if(isHigherVersion() && canNotify())
			notifyServer();
	}
	private void tryNotifyDynious(){
		if(isHigherVersion()){
			NBTTagCompound update = new NBTTagCompound();
			update.setString("modDisplayName", HostMODNAME);
			update.setString("oldVersion", HostVERSION);
			update.setString("newVersion", latest);
			update.setString("updateURL", getDownloadUrl());
			update.setBoolean("isDirectLink", false);
			FMLInterModComms.sendRuntimeMessage(HostMODID, "VersionChecker", "addUpdate", update);
		}
	}

	private String getVersionFromJson(){
		try{
			File file = new File(cachedir, curseCode+".json");
			if(file.exists()) {
				BufferedReader in = new BufferedReader(new FileReader(file));
				String contents = in.readLine().toLowerCase();
				int jarindex = contents.indexOf(HostMODNAME.toLowerCase()+" ");
				int versionindex = jarindex + HostMODNAME.length()+1;
				int dotjarindex = contents.indexOf(".jar");
				String versionnumber = contents.substring(versionindex, dotjarindex);
				in.close();
				return versionnumber;
			}
		}catch(IOException e){}
		return "0.0.0.0";
	}

	private final File cachedir = new File(((File)FMLInjectionData.data()[6]).getAbsolutePath().substring(0,((File)FMLInjectionData.data()[6]).getAbsolutePath().length()-2), "cachedjsons/");
	private void cacheJson(){
		File file = new File(cachedir, curseCode+".json");
		try{
			Files.createDirectory(cachedir.toPath());
		}catch(IOException e){}
		try{
			URL url = new URL(String.format("https://widget.mcf.li/mc-mods/minecraft/%s.json", curseCode));
			InputStream is = url.openStream();
			if(file.exists())
				file.delete();
			file.createNewFile();
			OutputStream os = new FileOutputStream(file);

			byte[] b = new byte[2048];
			int length;

			while((length = is.read(b)) != -1)
				os.write(b, 0, length);

			is.close();
			os.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	@Override
	public void initialize(Minecraft minecraftInstance) {}

	@Override
	public Class<? extends GuiScreen> mainConfigGuiClass() {
		return VCCG.class;
	}

	@Override
	public Set<IModGuiFactory.RuntimeOptionCategoryElement> runtimeGuiCategories() {
		return null;
	}

	@Override
	public IModGuiFactory.RuntimeOptionGuiHandler getHandlerFor(
			IModGuiFactory.RuntimeOptionCategoryElement element) {
		return null;
	}
	public static class VCCG extends GuiConfig {
		public VCCG(GuiScreen parentScreen) {
			super(parentScreen, new ConfigElement(config.getCategory(Configuration.CATEGORY_GENERAL)).getChildElements(), MODID, false,
					false, GuiConfig.getAbridgedConfigPath(config.toString()));
		}
	}
}
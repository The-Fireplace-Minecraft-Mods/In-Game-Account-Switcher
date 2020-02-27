package com.github.mrebhan.ingameaccountswitcher.tools;

import net.minecraft.client.Minecraft;
import the_fireplace.iasencrypt.Standards;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.DosFileAttributes;
import java.util.ArrayList;
/**
 * @author mrebhan
 * @author The_Fireplace
 */
public class Config implements Serializable {
	public static final long serialVersionUID = 0xDEADBEEF;

	private static Config instance = null;

	private static final String configFileName = Standards.cfgn;

	private ArrayList<Pair<String, Object>> field_218893_c;

	public static Config getInstance() {
		return instance;
	}

	private Config() {
		this.field_218893_c = new ArrayList<Pair<String, Object>>();
		instance = this;
	}

	public void setKey(Pair<String, Object> key) {
		if (this.getKey(key.getValue1()) != null)
			this.removeKey(key.getValue1());
		field_218893_c.add(key);
		save();
	}

	public void setKey(String key, Object value) {
		this.setKey(new Pair<String, Object>(key, value));
	}

	public Object getKey(String key) {
		if(field_218893_c == null){
			System.out.println("Error: Config failed to load during PreInitialization. Loading now.");
			load();
		}
		for (Pair<String, Object> aField_218893_c : field_218893_c) {
			if (aField_218893_c.getValue1().equals(key))
				return aField_218893_c.getValue2();
		}

		return null;
	}

	private void removeKey(String key) {
		for (int i = 0; i < field_218893_c.size(); i++) {
			if (field_218893_c.get(i).getValue1().equals(key))
				field_218893_c.remove(i);
		}
	}

	public static void save() {
		saveToFile();
	}

	public static void load() {
		loadFromOld();
		readFromFile();
	}

	private static void readFromFile() {
		File f = new File(Standards.IASFOLDER, configFileName);
		if (f.exists()) {
			try {
				ObjectInputStream stream = new ObjectInputStream(new FileInputStream(f));
				instance = (Config) stream.readObject();
				stream.close();
			} catch (IOException e) {
				e.printStackTrace();
				instance = new Config();
				f.delete();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				instance = new Config();
				f.delete();
			}
		}
		if (instance == null)
			instance = new Config();
	}

	private static void saveToFile() {
		try{
			Path file = new File(Standards.IASFOLDER, configFileName).toPath();
			DosFileAttributes attr = Files.readAttributes(file, DosFileAttributes.class);
			DosFileAttributeView view = Files.getFileAttributeView(file, DosFileAttributeView.class);
			if(attr.isHidden())
				view.setHidden(false);
		}catch(NoSuchFileException e) {

		}catch(Exception e){
			e.printStackTrace();
		}
		try {
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(new File(Standards.IASFOLDER, configFileName)));
			out.writeObject(instance);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try{
			Path file = new File(Standards.IASFOLDER, configFileName).toPath();
			DosFileAttributes attr = Files.readAttributes(file, DosFileAttributes.class);
			DosFileAttributeView view = Files.getFileAttributeView(file, DosFileAttributeView.class);
			if(!attr.isHidden())
				view.setHidden(true);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private static void loadFromOld(){
		File f = new File(Minecraft.getMinecraft().gameDir, "user.cfg");
		if (f.exists()) {
			try {
				ObjectInputStream stream = new ObjectInputStream(new FileInputStream(f));
				instance = (Config) stream.readObject();
				stream.close();
				f.delete();
				System.out.println("Loaded data from old file");
			} catch (IOException e) {
				e.printStackTrace();
				f.delete();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				f.delete();
			}
		}
	}
}

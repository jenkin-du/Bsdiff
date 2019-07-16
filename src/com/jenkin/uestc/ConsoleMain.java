package com.jenkin.uestc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Scanner;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class ConsoleMain {

	public static void main(String[] args) {

		String rootPath = System.getProperty("user.dir") + "//";
		File jsonFile = new File(rootPath + "output.json");

		Scanner scanner = new Scanner(System.in);

		if (jsonFile.exists()) {
			// 读取json文件
			String jsonString = readFile(jsonFile.getAbsolutePath());

			JSONArray jsonArr = JSON.parseArray(jsonString);
			JSONObject jsonData = jsonArr.getJSONObject(0);
			JSONObject apkData = jsonData.getJSONObject("apkData");
			String newApkFileName = apkData.getString("outputFile");

			File newVerionFile = new File(rootPath + newApkFileName);
			if (newVerionFile.exists()) {
				File folder = new File(rootPath);
				if (folder.isDirectory()) {

					File[] files = folder.listFiles();
					ArrayList<File> oldVersionFiles = new ArrayList<>();

					// 找到符合共同前缀的apk文件
					String prefix = newApkFileName.split("_")[0];
					for (int i = 0; i < files.length; i++) {

						String oldName = files[i].getName();
						if (oldName.startsWith(prefix) && !oldName.equals(newApkFileName) && oldName.endsWith(".apk")) {
							// 判断是否存在比output.json读取的版本更高的apk,出错的原因是apk拷贝到该，目录下，但output.json没有改变
							if (judgeIsOldOne(files[i].getName(), newApkFileName)) {
								oldVersionFiles.add(files[i]);
							}
						}
					}

					if (oldVersionFiles.size() != 0) {
						// 展示出新旧版本
						System.out.println("新版本文件：");
						System.out.println("    " + newApkFileName);
						System.out.println("旧版本文件：");
						for (File file : oldVersionFiles) {
							System.out.println("    " + file.getName());
						}

						// 交互，是否进行新旧版本的差分
						System.out.println("是否生成补丁文件？");
						System.out.println(
								"按1：是                                                                   按0：否(退出)");

						String line = scanner.nextLine();
						int re = Integer.valueOf(line);
						while (re != 1 && re != 0) {
							System.out.println("请输入正确的数字");

							line = scanner.nextLine();
							re = Integer.valueOf(line);
						}

						if (re == 1) {
							System.out.println("请稍等，正在生成补丁文件");
							System.out.print("====");
							for (File file : oldVersionFiles) {
								String oldVerion = getVerionName(file.getName());
								String newVerion = getVerionName(newApkFileName);
								String patchName = newApkFileName.split("_")[0] + "_patch_v" + oldVerion + "-"
										+ newVerion + ".ph";

								// 进行差分运算
								BsDiff.genDiff(file.getAbsolutePath(), newVerionFile.getAbsolutePath(),
										rootPath + patchName);
								System.out.print("====");
							}
							System.out.print("\n");
							System.out.println("补丁文件生成成功！！！");
							scanner.nextLine();
						}

					} else {
						System.out.println("没有旧版本文件！");
						scanner.nextLine();
					}
				} else {
					System.out.println("根目录错误！");
					scanner.nextLine();
				}
			} else {
				System.out.println("新版本不存在！");
				scanner.nextLine();
			}
		} else {
			System.out.println("没有新版本的json文件");
			scanner.nextLine();
		}

	}

	/**
	 * 判断是否是老版本
	 * 
	 * @param oldName
	 * @param newName
	 * @return
	 */
	private static boolean judgeIsOldOne(String oldName, String newName) {

		String oldVerion = getVerionName(oldName);
		String newVerion = getVerionName(newName);

		boolean isOld = compareVersion(newVerion, oldVerion);
		return isOld;
	}

	/**
	 * 获取文件的版本号
	 * 
	 * @param fileName
	 * @return
	 */
	public static String getVerionName(String fileName) {

		String verion = fileName.split("_")[1];
		verion = verion.substring(1, verion.length());
		verion = verion.substring(0, verion.lastIndexOf("."));

		return verion;
	}

	/**
	 * 读文件
	 * 
	 * @param fileName
	 * @return
	 */
	private static String readFile(String fileName) {

		String encoding = "UTF-8";
		File file = new File(fileName);
		Long filelength = file.length();
		byte[] filecontent = new byte[filelength.intValue()];
		try {
			FileInputStream in = new FileInputStream(file);
			in.read(filecontent);
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			return new String(filecontent, encoding);
		} catch (UnsupportedEncodingException e) {
			System.err.println("The OS does not support " + encoding);
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 比较服务器上的版本是否比客户端的版本高
	 * 
	 * @param version
	 *            服务器上的版本
	 * @param oldVersion
	 *            客户端上的版本
	 * @return
	 */
	private static boolean compareVersion(String version, String oldVersion) {

		String[] versions = version.split("\\.");
		String[] oldVersions = oldVersion.split("\\.");

		if (versions.length == 3 && versions.length == oldVersions.length) {
			if (Integer.valueOf(versions[0]) > Integer.valueOf(oldVersions[0])) {
				return true;
			} else if ((Integer.valueOf(versions[0]) == Integer.valueOf(oldVersions[0]))
					&& (Integer.valueOf(versions[1]) > Integer.valueOf(oldVersions[1]))) {
				return true;
			} else if ((Integer.valueOf(versions[0]) == Integer.valueOf(oldVersions[0]))
					&& (Integer.valueOf(versions[1]) == Integer.valueOf(oldVersions[1]))
					&& (Integer.valueOf(versions[2]) > Integer.valueOf(oldVersions[2]))) {
				return true;
			}
		}

		return false;
	}

}

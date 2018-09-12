import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import static java.io.File.separator;

/**
 * Create by Brendanlu at 2018/6/7.
 */
public class GenerateToString {

	/**
	 * 定义所有要修改的javabean集合
	 */
	private static List<String> filelist = new ArrayList<String>();

	/**
	 * 添加绝对路径所需部分路劲
	 */
	private static final String PATH_APPEND =   separator + "src" + separator + "main" + separator + "java" + separator;

	/**
	 * 扫描包
	 */
	private static String PACKAGE_PATH = "";

	/**
	 * 匹配类型
	 */
	private static final String FILE_TYPE = ".java";

	/**
	 * 父类类型
	 */
	private static final String SUPER_CLASS = "";

	/**
	 * 排除的方法
	 */
	private static final String EXCEPT = "";

	/**
	 * 是否含有toString 方法标志
	 */
	private static final String FLAG = "public String toString() {";

	/**
	 *
	 * @Title: readLine
	 * @Description: 按行读取文件
	 * @param @return
	 * @param @throws IOException 方法参数
	 * @return String 返回类型
	 * @throws 异常信息
	 */
	private static StringBuilder readFileWithToString(String pathAndName) throws IOException {
		boolean contentFlag = false;
		StringBuilder sb = new StringBuilder("");
		ArrayListRemove<String> list = new ArrayListRemove<String>();
		//        FileReader reader = new FileReader(pathAndName);
		InputStreamReader reader = new InputStreamReader(new FileInputStream(pathAndName), Charset.forName("UTF-8"));
		BufferedReader br = new BufferedReader(reader);
		String str = null;
		int mark = 0;
		int startMark = 0;
		int endMark = 0;
		int end = 0;
		while ((str = br.readLine()) != null) {
			if (str.trim().equals(FLAG)) {
				contentFlag = true;
				if (list.get(mark - 1).contains("@Override")) {
					startMark = mark - 1;
				} else {
					startMark = mark;
				}
			}
			sb.append(str + "\n");
			list.add(str + "\n");
			mark++;
		}
		for (int i = startMark; i < list.size(); i++) {
			if (list.get(i).contains("return")) {
				endMark = i;
			}
			if (endMark > 0 && list.get(i).contains("}")) {
				endMark = i;
				break;
			}
			//            if(endMark>0&&!list.get(i).trim().equals("")){
			//                endMark=i;
			//            }
		}
		br.close();
		reader.close();
		StringBuilder returnSb = null;
		if (!contentFlag) {
			returnSb = new StringBuilder(sb.reverse().toString().trim().replaceFirst("}", "").trim()).reverse();
		} else {
			list.removeRange(startMark, endMark + 1);
			sb = new StringBuilder("");
			for (String listVar : list) {
				sb.append(listVar);
			}
			returnSb = new StringBuilder(sb.reverse().toString().trim().replaceFirst("}", "").trim()).reverse();
		}
		return returnSb;
	}

	/**
	 *
	 * @Title: readLine
	 * @Description: 按行读取文件
	 * @param @return
	 * @param @throws IOException 方法参数
	 * @return String 返回类型
	 * @throws 异常信息
	 */
	private static ClassBean readFileWithoutToString(String pathAndName) throws IOException {
		boolean contentFlag = false;
		ClassBean returnBean = new ClassBean();
		StringBuilder sb = new StringBuilder("");
		List<String> list = new ArrayList<String>();
		//        FileReader reader = new FileReader(pathAndName);
		InputStreamReader reader = new InputStreamReader(new FileInputStream(pathAndName), Charset.forName("UTF-8"));
		BufferedReader br = new BufferedReader(reader);
		String str = null;
		while ((str = br.readLine()) != null) {
			sb.append(str + "\n");
			list.add(str + "\n");
			if (str.contains("toString")) {
				returnBean.haveToString = true;
				return returnBean;
			}
		}
		br.close();
		reader.close();
		StringBuilder returnSb = null;
		if (!contentFlag) {
			returnSb = new StringBuilder(sb.reverse().toString().replaceFirst("}", "").trim()).reverse();
		} else {
			returnSb = new StringBuilder(sb.reverse().toString().replaceFirst("edirrevO@", "").trim()).reverse();
		}
		returnBean.haveToString = false;
		returnBean.classString = returnSb;
		return returnBean;
	}

	/**
	 *
	 * @Title: writeFile
	 * @Description: 写入文件
	 * @param @param pathAndName
	 * @param @param content
	 * @param @throws IOException 方法参数
	 * @return void 返回类型
	 * @throws 异常信息
	 */
	private static void writeFile(String pathAndName, String content) throws IOException {
		//        FileWriter writer = new FileWriter(pathAndName);
		OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(pathAndName), Charset.forName("UTF-8"));
		BufferedWriter bw = new BufferedWriter(writer);
		bw.write(content);
		bw.close();
		writer.close();
	}

	/**
	 *
	 * @Title: toString
	 * @Description: 反射生成文件的toString 方法
	 * @param @param pathAndName
	 * @param @param content
	 * @param @throws IOException 方法参数
	 * @return void 返回类型
	 * @throws ClassNotFoundException
	 * @throws 异常信息
	 */
	private static StringBuilder toString(String pathAndName) throws ClassNotFoundException, MalformedURLException {
		StringBuilder sbType = new StringBuilder();
		sbType.append("\n\n");
		sbType.append("\t@Override\n");
		sbType.append("\t" + "public\tString\ttoString() { " + "\n\n");
		File f = new File(pathAndName.substring(0, pathAndName.indexOf(PATH_APPEND) + PATH_APPEND.length()));
		URL[] cp = { f.toURI().toURL() };
		URLClassLoader urlcl = new URLClassLoader(cp);
		Class<?> classAllName = urlcl.loadClass((pathAndName.substring(pathAndName.indexOf(PATH_APPEND) + PATH_APPEND.length(), pathAndName.indexOf(FILE_TYPE))).replace(separator, "."));
		sbType.append("\t \t return ");
		sbType.append("\"" + classAllName.getSimpleName() + " { ");
		Class<?> superClass = classAllName.getSuperclass();
		if (superClass.getName().equals(SUPER_CLASS)) {
			Field[] fields = classAllName.getDeclaredFields();
			for (int j = 0; j < fields.length; j++) {
				if (EXCEPT.isEmpty()||EXCEPT.length()==0||fields[j].getName().equals(EXCEPT) || fields[j].getName().contains(EXCEPT)) {
					continue;
				}
				sbType.append(fields[j].getName() + "= \"+ ");
				//				if(fields[j].getType().isPrimitive()||fields[j].getType().equals(String.class)){
				sbType.append(fields[j].getName() + "+\",");
				//				}else  {
				//妈耶  不用增加tostring方法 java会自动调用
				//					sbType.append(fields[j].getName() + ".toString()+\",");
				//				}
			}
			if (sbType.charAt(sbType.length() - 1) == ',') {
				sbType.deleteCharAt(sbType.length() - 1);
			}
			//			if(sbType.charAt(sbType.length()-1)=='\"'){
			//				sbType.deleteCharAt(sbType.length()-1);
			//			}
			//			if(sbType.charAt(sbType.length()-1)=='+'){
			//				sbType.deleteCharAt(sbType.length()-1);
			//			}
			sbType.append(" }\"");
			sbType.append(";\n");
			sbType.append("\t}\n\n");
			sbType.append("}");
			return sbType;
		} else {
			return new StringBuilder("\n\n}");
		}
	}

	/**
	 *
	 * @Title: getFiles
	 * @Description: 通过递归得到某一路径下所有的目录及其文件
	 * @param @param filePath 方法参数
	 * @return void 返回类型
	 * @throws 异常信息
	 */
	private static void getFiles(String filePath) {
		File root = new File(filePath);
		File[] files = root.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				getFiles(file.getAbsolutePath());
			} else {
				if (file.getName().endsWith(FILE_TYPE)) {
					filelist.add(file.getAbsolutePath());
				}
			}
		}
	}

	/**
	 *
	 * @Title: create
	 * @Description: 通过文件路径生成
	 * @param @param projectDir
	 * @param @throws IOException 方法参数
	 * @return void 返回类型
	 * @throws ClassNotFoundException
	 * @throws 异常信息
	 */
	private static void create(String projectDir) throws IOException, ClassNotFoundException {
		getFiles(projectDir + PATH_APPEND + PACKAGE_PATH.replace(".", "/") + "/");
		ClassBean fileContent = new ClassBean();
		for (String files : filelist) {
			fileContent = readFileWithoutToString(files);
			if (!fileContent.haveToString) {
				fileContent.classString.append(toString(files));
				writeFile(files, fileContent.classString.toString());
			}
		}
	}

	/**
	 *
	 * @Title: toLowerCaseFirstOne
	 * @Description: 首字母转小写
	 * @param @param s
	 * @param @return 方法参数
	 * @return String 返回类型
	 * @throws 异常信息
	 */
	private static String toLowerCaseFirstOne(String s) {
		if (Character.isLowerCase(s.charAt(0)))
			return s;
		else
			return (new StringBuilder()).append(Character.toLowerCase(s.charAt(0))).append(s.substring(1)).toString();
	}

	/**
	 *
	 * @Title: toUpperCaseFirstOne
	 * @Description: 首字母转大写
	 * @param @param s
	 * @param @return 方法参数
	 * @return String 返回类型
	 * @throws 异常信息
	 */
	private static String toUpperCaseFirstOne(String s) {
		if (Character.isUpperCase(s.charAt(0)))
			return s;
		else
			return (new StringBuilder()).append(Character.toUpperCase(s.charAt(0))).append(s.substring(1)).toString();
	}

	static class ArrayListRemove<E> extends ArrayList<E> {

		public boolean contains(Object o) {
			return super.contains(o);
		}

		public void removeRange(int fromIndex, int toIndex) {
			super.removeRange(fromIndex, toIndex);
		}
	}

	protected static class ClassBean {

		StringBuilder classString;

		Boolean haveToString;

		public ClassBean() {
		}

		public ClassBean(StringBuilder classString) {
			this.classString = classString;
		}

		public ClassBean(StringBuilder classString, Boolean haveToString) {
			this.classString = classString;
			this.haveToString = haveToString;
		}
	}

	public static void main(String[] args) {
		genToString();
	}

	public static int genToString() {
		String projectDir = System.getProperty("user.dir");
		if (// Windows 7 获取到的路径不包括这一目录
				projectDir.contains("module-gencode"))
			projectDir = projectDir.substring(0, projectDir.length() - "module-gencode".length() - 1);
		try {
			create(projectDir);
			return 0;
		} catch (Exception e) {
			System.out.println("创建ToString方法失败");
			System.out.println(e.toString());
			return -1;
		}
	}
}

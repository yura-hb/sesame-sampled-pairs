import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.URL;

class WebGet {
    /**
     * Creates a new instance of WebGet
     */
    static void url(String urls) throws Exception {
	Authenticator.setDefault(new MyAuthenticator());
	//Security.setProperty("auth.login.defaultCallbackHandler", "WebGet$Handler");
	URL url = new URL(urls);
	InputStream ins = url.openConnection().getInputStream();
	BufferedReader reader = new BufferedReader(new InputStreamReader(ins));
	String str;
	while ((str = reader.readLine()) != null)
	    System.out.println(str);
    }

    class MyAuthenticator extends Authenticator {
	public MyAuthenticator() {
	    super();
	}

    }

}


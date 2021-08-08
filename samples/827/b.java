import com.sun.security.auth.module.Krb5LoginModule;
import java.util.HashMap;
import java.util.Map;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;

class Context {
    /**
     * Logins with username/password as an existing Subject. The
     * same subject can be used multiple times to simulate multiple logins.
     */
    public static Context fromUserPass(Subject s, String user, char[] pass, boolean storeKey) throws Exception {
	Context out = new Context();
	out.name = user;
	out.s = s;
	Krb5LoginModule krb5 = new Krb5LoginModule();
	Map&lt;String, String&gt; map = new HashMap&lt;&gt;();
	Map&lt;String, Object&gt; shared = new HashMap&lt;&gt;();

	if (storeKey) {
	    map.put("storeKey", "true");
	}

	if (pass != null) {
	    krb5.initialize(out.s, new CallbackHandler() {
		@Override
		public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
		    for (Callback cb : callbacks) {
			if (cb instanceof NameCallback) {
			    ((NameCallback) cb).setName(user);
			} else if (cb instanceof PasswordCallback) {
			    ((PasswordCallback) cb).setPassword(pass);
			}
		    }
		}
	    }, shared, map);
	} else {
	    map.put("doNotPrompt", "true");
	    map.put("useTicketCache", "true");
	    if (user != null) {
		map.put("principal", user);
	    }
	    krb5.initialize(out.s, null, shared, map);
	}

	krb5.login();
	krb5.commit();

	return out;
    }

    private String name;
    private Subject s;

    private Context() {
    }

}


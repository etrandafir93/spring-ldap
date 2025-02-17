/*
 * Copyright 2005-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ldap.test.unboundid;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

final class SocketUtils {

	private SocketUtils() {
	}

	static boolean isPortOpen(int port) {
		try (Socket ignored = new Socket("localhost", port)) {
			return true;
		}
		catch (IOException ex) {
			return false;
		}
	}

	static int getFreePort() {
		try (ServerSocket serverSocket = new ServerSocket(0)) {
			return serverSocket.getLocalPort();
		}
		catch (IOException ex) {
			throw new RuntimeException("Could not get a free port", ex);
		}
	}

}

package com.asksunny.server;

import com.asksunny.cli.utils.annotation.CLIOptionBinding;

public class ServerConfig {

	@CLIOptionBinding(shortOption='p', longOption="port", hasValue=true, description="Server TCP port number")
	int port;
	
	@CLIOptionBinding(shortOption='b', longOption="binding-address", hasValue=true, description="Server binding address, default 0.0.0.0")
	String bindingAddress = null;
	
	
	public ServerConfig() {
		
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getBindingAddress() {
		return bindingAddress;
	}

	public void setBindingAddress(String bindingAddress) {
		this.bindingAddress = bindingAddress;
	}
	
	
	

	
}

/*
 * This file is part of JSTUN. 
 * 
 * Copyright (c) 2005 Thomas King <king@t-king.de>
 *
 * JSTUN is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * JSTUN is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JSTUN; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.javawi.jstun.test;

import java.net.*;

public class DiscoveryInfo {
	private boolean error = false;
	private int errorResponseCode = 0;
	private String errorReason;
	private boolean openAccess = false;
	private boolean blockedUDP = false;
	private boolean fullCone = false;
	private boolean restrictedCone = false;
	private boolean portRestrictedCone = false;
	private boolean symmetricCone = false;
	private boolean symmetricUDPFirewall = false;
	private InetAddress publicIP;
	
	public DiscoveryInfo() {
		super();
	}
	
	public boolean isError() {
		return error;
	}
	
	public void setError(int responseCode, String reason) {
		this.error = true;
		this.errorResponseCode = responseCode;
		this.errorReason = reason;
	}
	
	public boolean isOpenAccess() {
		if (error) return false;
		return openAccess;
	}

	public void setOpenAccess() {
		this.openAccess = true;
	}

	public boolean isBlockedUDP() {
		if (error) return false;
		return blockedUDP;
	}

	public void setBlockedUDP() {
		this.blockedUDP = true;
	}
	
	public boolean isFullCone() {
		if (error) return false;
		return fullCone;
	}

	public void setFullCone() {
		this.fullCone = true;
	}

	public boolean isPortRestrictedCone() {
		if (error) return false;
		return portRestrictedCone;
	}

	public void setPortRestrictedCone() {
		this.portRestrictedCone = true;
	}

	public boolean isRestrictedCone() {
		if (error) return false;
		return restrictedCone;
	}

	public void setRestrictedCone() {
		this.restrictedCone = true;
	}

	public boolean isSymmetricCone() {
		if (error) return false;
		return symmetricCone;
	}

	public void setSymmetricCone() {
		this.symmetricCone = true;
	}

	public boolean isSymmetricUDPFirewall() {
		if (error) return false;
		return symmetricUDPFirewall;
	}

	public void setSymmetricUDPFirewall() {
		this.symmetricUDPFirewall = true;
	}
	
	public InetAddress getPublicIP() {
		return publicIP;
	}
	
	public void setPublicIP(InetAddress publicIP) {
		this.publicIP = publicIP;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if (error) {
			sb.append(errorReason + " - Responsecode: " + errorResponseCode);
			return sb.toString();
		}
		if (openAccess) sb.append("Open access to the Internet. ");
		if (blockedUDP) sb.append("Firewall blocks UDP. ");
		if (fullCone) sb.append("Full Cone NAT handles connections. ");
		if (restrictedCone) sb.append("Restricted Cone NAT handles connections. ");
		if (portRestrictedCone) sb.append("Port restricted Cone NAT handles connections." );
		if (symmetricCone) sb.append("Symmetric Cone NAT handles connections. ");
		if (symmetricUDPFirewall) sb.append ("Symmetric UDP Firewall handles connections. ");
		sb.append("Public IP:");
		sb.append(publicIP.toString());
		return sb.toString();
	}	
}

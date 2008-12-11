/*
 * Tigase Jabber/XMPP Server
 * Copyright (C) 2004-2008 "Artur Hefczyc" <artur.hefczyc@tigase.org>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://www.gnu.org/licenses/.
 * 
 * $Rev$
 * Last modified by $Author$
 * $Date$
 */

package tigase.server.sreceiver.sysmon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Queue;
import java.util.logging.Logger;
import tigase.server.Packet;
import tigase.util.OSUtils;

/**
 * Created: Dec 10, 2008 8:14:53 PM
 *
 * @author <a href="mailto:artur.hefczyc@tigase.org">Artur Hefczyc</a>
 * @version $Rev$
 */
public class DiskMonitor extends AbstractMonitor {

  private static final Logger log =
    Logger.getLogger("tigase.server.sreceiver.sysmon.DiskMonitor");

	private File[] roots = null;

	@Override
	public void init(String jid, double treshold, SystemMonitorTask smTask) {
		super.init(jid, treshold, smTask);
		roots = File.listRoots();
		findAllRoots();
	}

	private void findAllRoots() {
		switch (OSUtils.getOSType()) {
			case windows:
				File[] winRoots = File.listRoots();
				roots = winRoots;
				break;
			case linux:
				File[] linRoots = getLinuxRoots();
				roots = linRoots;
				break;
			case solaris:
				File[] solRoots = getSolarisRoots();
				roots = solRoots;
				break;
			case mac:
				File[] macRoots = getMacRoots();
				roots = macRoots;
				break;
			default:
				File[] otherRoots = File.listRoots();
				if (otherRoots.length == 1) {
					File[] mtabRoots = getLinuxRoots();
					if (mtabRoots != null && mtabRoots.length > 1) {
						otherRoots = mtabRoots;
					}
					roots = otherRoots;
				}
		}
	}

	@Override
	public void check1Min(Queue<Packet> results) {
		for (File file : roots) {
			if (file.canWrite()) {
				NumberFormat format = NumberFormat.getIntegerInstance();
				if (format instanceof DecimalFormat) {
					DecimalFormat decf = (DecimalFormat) format;
					decf.applyPattern(decf.toPattern() + " KB");
				}
				NumberFormat formp = NumberFormat.getPercentInstance();
				formp.setMaximumFractionDigits(2);
				double percent = new Long(file.getUsableSpace()).doubleValue() /
								new Long(file.getTotalSpace()).doubleValue();
				if (file.getUsableSpace() < file.getTotalSpace() * treshold) {
					prepareWarning("Available space on volume: " + file.toString() +
									" is low: " + format.format(file.getUsableSpace()/1024) +
									" of " + format.format(file.getTotalSpace()/1024) +
									" - " + formp.format(percent),
									results, file);
				} else {
					prepareCalmDown("Available space on volume: " + file.toString() +
									" is OK now: " + format.format(file.getUsableSpace()/1024) +
									" of " + format.format(file.getTotalSpace()/1024) +
									" - " + formp.format(percent),
									results, file);
				}
			}
		}
	}

	@Override
	public void check1Hour(Queue<Packet> results) {
		findAllRoots();
	}

	public String getState() {
		StringBuilder sb = new StringBuilder();
		Formatter formatter = new Formatter(sb);
		String format_s = "%1$15s%2$12s%3$12s%4$12s%5$12s\n";
		formatter.format(format_s, "Filesystem", "Size", "Used", "Avail", "Free%");
		String format_n = "%1$15s%2$,12dM%3$,12dM%4$,12dM%5$12.2f%%\n";
		long MEGA = 1024*1024;
		for (File file : roots) {
			if (file.canWrite()) {
				double percent = new Long(file.getUsableSpace()).doubleValue() /
								new Long(file.getTotalSpace()).doubleValue();
				formatter.format(format_n, file.toString(),
								(file.getTotalSpace() / MEGA), 
								(file.getTotalSpace() - file.getUsableSpace())/MEGA,
								(file.getUsableSpace()/MEGA), percent*100);
			}
		}
		return formatter.toString();
	}

	private File[] getLinuxRoots() {
		try {
			BufferedReader buffr = new BufferedReader(new FileReader("/etc/mtab"));
			String line = null;
			ArrayList<File> results = new ArrayList<File>();
			while ((line = buffr.readLine()) != null) {
				if (line.contains("proc") || line.contains("dev") || 
								line.contains("tmpfs") || line.contains("sysfs")) {
					continue;
				}
				String[] parts = line.split("\\s");
				results.add(new File(parts[1]));
			}
			return results.toArray(new File[results.size()]);
		} catch (Exception e) {
			log.warning("Can not read filesystems from /etc/mtab file" + e);
			return File.listRoots();
		}

	}

	private File[] getMacRoots() {
		File volumes = new File("/Volumes");
		return volumes.listFiles(new FileFilter() {
			public boolean accept(File path) {
				return path.isDirectory();
			}
		});
	}

	private File[] getSolarisRoots() {
		throw new UnsupportedOperationException("Not yet implemented");
	}

}
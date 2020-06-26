package org.apex.utils;

import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;

/**
 * @author WangYi
 * @since 2020/6/27
 */
public final class IpUtils {

  /**
   * Get local IPv4 address
   *
   * @return IPv4 Address
   * @throws SocketException Thrown to indicate that there is
   *                         an error creating or accessing a Socket.
   */
  public static Optional<Inet4Address> getLocalIp4Address() throws SocketException {
    final List<Inet4Address> ipByNi = getLocalIp4AddressFromNetworkInterface();
    if (ipByNi.size() != 1) {
      final Optional<Inet4Address> ipBySocketOpt = getIpBySocket();
      if (ipBySocketOpt.isPresent()) {
        return ipBySocketOpt;
      } else {
        return ipByNi.isEmpty() ? Optional.empty() : Optional.of(ipByNi.get(0));
      }
    }
    return Optional.of(ipByNi.get(0));
  }

  /**
   * Get local IPv4 address through Network Interface
   *
   * @return IPv4 Address
   * @throws SocketException Thrown to indicate that there is
   *                         an error creating or accessing a Socket.
   */
  private static List<Inet4Address> getLocalIp4AddressFromNetworkInterface() throws SocketException {
    List<Inet4Address> addresses = new ArrayList<>(1);
    Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
    if (e == null) {
      return addresses;
    }
    while (e.hasMoreElements()) {
      NetworkInterface n = e.nextElement();
      if (!isValidInterface(n)) {
        continue;
      }
      Enumeration<InetAddress> ee = n.getInetAddresses();
      while (ee.hasMoreElements()) {
        InetAddress i = ee.nextElement();
        if (isValidAddress(i)) {
          addresses.add((Inet4Address) i);
        }
      }
    }
    return addresses;
  }

  /**
   * Access 8.8.8.8 through socket to get local IPv4 address
   *
   * @return IPv4 Address
   * @throws SocketException Thrown to indicate that there is
   *                         an error creating or accessing a Socket.
   */
  public static Optional<Inet4Address> getIpBySocket() throws SocketException {
    try (final DatagramSocket socket = new DatagramSocket()) {
      socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
      if (socket.getLocalAddress() instanceof Inet4Address) {
        return Optional.of((Inet4Address) socket.getLocalAddress());
      }
    } catch (UnknownHostException e) {
      throw new RuntimeException(e);
    }
    return Optional.empty();
  }

  /**
   * Filter loopback network cards, point-to-point network
   * cards, inactive network cards, virtual network cards
   * and require the name of the network card to start with
   * eth or ens
   *
   * @param ni Network card
   * @return True if the requirements are met, otherwise false
   */
  private static boolean isValidInterface(NetworkInterface ni) throws SocketException {
    return !ni.isLoopback() && !ni.isPointToPoint() && ni.isUp() && !ni.isVirtual()
            && (ni.getName().startsWith("eth") || ni.getName().startsWith("ens"));
  }

  /**
   * Determine whether it is I Pv 4 and the internal network
   * address and filter the loopback address.
   */
  private static boolean isValidAddress(InetAddress address) {
    return address instanceof Inet4Address && address.isSiteLocalAddress() && !address.isLoopbackAddress();
  }
}

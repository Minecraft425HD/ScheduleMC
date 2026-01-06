/**
 * Network packet system for client-server communication using Forge's networking API.
 *
 * <h2>Overview</h2>
 * <p>This package handles all client-server communication through custom network packets.
 * Packets are registered with Forge's SimpleChannel and use custom codecs for serialization.</p>
 *
 * <h2>Packet Categories</h2>
 * <ul>
 *   <li><strong>Plot Packets:</strong> Plot creation, trust management, sale/rent, abandonment</li>
 *   <li><strong>Economy Packets:</strong> Transactions, balance sync, loan operations</li>
 *   <li><strong>NPC Packets:</strong> NPC spawning, schedule updates, AI control</li>
 *   <li><strong>Production Packets:</strong> Production start/stop, harvest, processing</li>
 *   <li><strong>Utility Packets:</strong> Utility consumption sync, threshold updates</li>
 *   <li><strong>Crime Packets:</strong> Crime reporting, bounty updates, arrest notifications</li>
 * </ul>
 *
 * <h2>Packet Handlers</h2>
 * <p>Network handlers manage packet registration and routing:</p>
 * <ul>
 *   <li><strong>PlotNetworkHandler:</strong> Plot-related packet registration</li>
 *   <li><strong>NPCNetworkHandler:</strong> NPC packet registration</li>
 *   <li><strong>EconomyNetworkHandler:</strong> Economy packet registration</li>
 * </ul>
 *
 * <h2>Security</h2>
 * <ul>
 *   <li>All packets validate player permissions before execution</li>
 *   <li>Input validation using {@link de.rolandsw.schedulemc.util.InputValidation}</li>
 *   <li>Transaction rollback on packet handling failures</li>
 *   <li>Rate limiting to prevent packet spam</li>
 * </ul>
 *
 * <h2>Example Packet Flow</h2>
 * <pre>
 * Client (Button Click)
 *     ↓
 * PlotSalePacket.sendToServer(plotId, price, type)
 *     ↓
 * Server receives → PlotSalePacket.handle()
 *     ↓
 * Permission check → PlotManager.setSalePrice()
 *     ↓
 * Sync packet sent back to all players
 * </pre>
 *
 * @since 1.0
 * @see de.rolandsw.schedulemc.util.PacketHandler
 */
package de.rolandsw.schedulemc.network;

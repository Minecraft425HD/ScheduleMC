#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Translation Key Addition Script for ScheduleMC
Adds ALL hardcoded German strings as translation keys to de_de.json and en_us.json
"""

import json
import os
from pathlib import Path

# Base path for language files
LANG_PATH = Path("src/main/resources/assets/schedulemc/lang")
DE_FILE = LANG_PATH / "de_de.json"
EN_FILE = LANG_PATH / "en_us.json"

# Complete translation mapping: key -> (German, English)
TRANSLATIONS = {
    # ========================================
    # PlotAppScreen.java - Plot/Immobilien App
    # ========================================
    "gui.app.plot.tab.plot": ("Plot", "Plot"),
    "gui.app.plot.tab.market": ("Markt", "Market"),
    "gui.app.plot.tab.mine": ("Meine", "Mine"),
    "gui.app.plot.tab.finances": ("Geld", "Money"),

    "gui.app.plot.title": ("§6§lImmobilien", "§6§lReal Estate"),
    "gui.app.plot.no_plot": ("§7Kein Plot gefunden", "§7No plot found"),
    "gui.app.plot.stand_on_plot": ("§8Stehe auf einem Plot", "§8Stand on a plot"),
    "gui.app.plot.owner": ("§7Besitzer: §f", "§7Owner: §f"),
    "gui.app.plot.no_owner": ("§cKein Besitzer", "§cNo owner"),
    "gui.app.plot.size": ("§7Größe: §e", "§7Size: §e"),
    "gui.app.plot.blocks": (" Blöcke", " blocks"),
    "gui.app.plot.for_sale": ("§a⚡ ZUM VERKAUF", "§a⚡ FOR SALE"),
    "gui.app.plot.price": ("§7Preis: §e", "§7Price: §e"),
    "gui.app.plot.for_rent": ("§d⚡ ZU VERMIETEN", "§d⚡ FOR RENT"),
    "gui.app.plot.rent": ("§7Miete: §e", "§7Rent: §e"),
    "gui.app.plot.consumption": ("§b§l⚡ VERBRAUCH", "§b§l⚡ CONSUMPTION"),
    "gui.app.plot.electricity": ("§e⚡ Strom:", "§e⚡ Power:"),
    "gui.app.plot.avg_7day": ("§8  7-Tage-Ø: ", "§8  7-day avg: "),
    "gui.app.plot.water": ("§b💧 Wasser:", "§b💧 Water:"),
    "gui.app.plot.devices": ("§7Geräte: §f", "§7Devices: §f"),
    "gui.app.plot.no_offers": ("§7Keine Angebote", "§7No offers"),
    "gui.app.plot.offers": (" Angebote", " offers"),
    "gui.app.plot.buy_label": ("[KAUF]", "[BUY]"),
    "gui.app.plot.rent_label": ("[MIETE]", "[RENT]"),
    "gui.app.plot.no_plots": ("§7Du besitzt keine Plots", "§7You don't own any plots"),
    "gui.app.plot.buy_in_market": ("§8Kaufe einen im Markt-Tab!", "§8Buy one in the Market tab!"),
    "gui.app.plot.properties": (" Grundstücke", " properties"),
    "gui.app.plot.private": ("§7Privat", "§7Private"),
    "gui.app.plot.for_sale_short": ("[Verkauf]", "[Sale]"),
    "gui.app.plot.rented": ("[Vermietet]", "[Rented]"),
    "gui.app.plot.to_rent": ("[Zu vermieten]", "[To rent]"),
    "gui.app.plot.devices_count": (" Geräte", " devices"),
    "gui.app.plot.warning": ("§c§l⚠ WARNUNG", "§c§l⚠ WARNING"),
    "gui.app.plot.high_consumption": ("§7Hoher Verbrauch erkannt!", "§7High consumption detected!"),
    "gui.app.plot.bills": ("§6§l💰 RECHNUNGEN", "§6§l💰 BILLS"),
    "gui.app.plot.no_properties": ("§8Keine Grundstücke", "§8No properties"),
    "gui.app.plot.total_avg": ("§fGesamt (7-Tage-Ø/Tag)", "§fTotal (7-day avg/day)"),
    "gui.app.plot.sum": ("§f§lSUMME:", "§f§lTOTAL:"),
    "gui.app.plot.per_property": ("§8Pro Grundstück:", "§8Per property:"),
    "gui.app.plot.history_7day": ("§6§l📊 7-TAGE VERLAUF", "§6§l📊 7-DAY HISTORY"),
    "gui.app.plot.today": ("Heute", "Today"),
    "gui.app.plot.yesterday": ("Gestern", "Yesterday"),
    "gui.app.plot.days_ago_2": ("Vor 2d", "2d ago"),
    "gui.app.plot.days_ago_3": ("Vor 3d", "3d ago"),
    "gui.app.plot.days_ago_4": ("Vor 4d", "4d ago"),
    "gui.app.plot.days_ago_5": ("Vor 5d", "5d ago"),
    "gui.app.plot.days_ago_6": ("Vor 6d", "6d ago"),
    "gui.app.plot.prices": ("§8Preise: 0.35€/kWh, 0.50€/100L", "§8Prices: 0.35€/kWh, 0.50€/100L"),

    # ========================================
    # SettingsAppScreen.java - Einstellungen App
    # ========================================
    "gui.app.settings.tab.plot": ("Plot", "Plot"),
    "gui.app.settings.tab.notification": ("Meldung", "Notification"),
    "gui.app.settings.tab.account": ("Konto", "Account"),

    "gui.app.settings.title": ("§f§lEinstellungen", "§f§lSettings"),
    "gui.app.settings.no_plot": ("§7Kein Plot gefunden", "§7No plot found"),
    "gui.app.settings.stand_on_plot": ("§8Stehe auf einem Plot", "§8Stand on a plot"),
    "gui.app.settings.not_your_plot": ("§c✗ Nicht dein Grundstück", "§c✗ Not your property"),
    "gui.app.settings.sale_rent": ("§e§l🏷 VERKAUF / MIETE", "§e§l🏷 SALE / RENT"),
    "gui.app.settings.status": ("§7Status: ", "§7Status: "),
    "gui.app.settings.for_sale": ("§a● Zum Verkauf", "§a● For Sale"),
    "gui.app.settings.rented": ("§e● Vermietet", "§e● Rented"),
    "gui.app.settings.to_rent": ("§d● Zu vermieten", "§d● To Rent"),
    "gui.app.settings.private": ("§7● Privat (nicht angeboten)", "§7● Private (not offered)"),
    "gui.app.settings.list_for_sale": ("§a🏷 Zum Verkauf stellen", "§a🏷 List for Sale"),
    "gui.app.settings.sale_price": ("Verkaufspreis", "Sale Price"),
    "gui.app.settings.enter_price": ("Preis in Euro eingeben:", "Enter price in Euro:"),
    "gui.app.settings.list_for_rent": ("§d🏠 Zur Miete stellen", "§d🏠 List for Rent"),
    "gui.app.settings.rent_price": ("Mietpreis", "Rent Price"),
    "gui.app.settings.price_per_day": ("Preis pro Tag in Euro:", "Price per day in Euro:"),
    "gui.app.settings.end_offer": ("§c✗ Angebot beenden", "§c✗ End Offer"),
    "gui.app.settings.trusted_players": ("§b§l👥 TRUSTED PLAYERS", "§b§l👥 TRUSTED PLAYERS"),
    "gui.app.settings.no_trusted": ("§8Keine vertrauenswürdigen Spieler", "§8No trusted players"),
    "gui.app.settings.add_player": ("§b+ Spieler hinzufügen", "§b+ Add Player"),
    "gui.app.settings.trust_player": ("Spieler vertrauen", "Trust Player"),
    "gui.app.settings.enter_username": ("Spielername eingeben:", "Enter player name:"),
    "gui.app.settings.plot_name": ("§d§l✏ PLOT-NAME", "§d§l✏ PLOT NAME"),
    "gui.app.settings.current": ("§7Aktuell: §f", "§7Current: §f"),
    "gui.app.settings.rename": ("§e✏ Umbenennen", "§e✏ Rename"),
    "gui.app.settings.rename_plot": ("Plot umbenennen", "Rename Plot"),
    "gui.app.settings.enter_new_name": ("Neuen Namen eingeben:", "Enter new name:"),
    "gui.app.settings.description": ("§6§l📝 BESCHREIBUNG", "§6§l📝 DESCRIPTION"),
    "gui.app.settings.no_description": ("§8Keine Beschreibung", "§8No description"),
    "gui.app.settings.change_description": ("§a📝 Beschreibung ändern", "§a📝 Change Description"),
    "gui.app.settings.description_label": ("Beschreibung", "Description"),
    "gui.app.settings.enter_description": ("Beschreibung eingeben:", "Enter description:"),
    "gui.app.settings.abandon_plot": ("§c§l🗑 PLOT AUFGEBEN", "§c§l🗑 ABANDON PLOT"),
    "gui.app.settings.warning_irreversible": ("§8⚠ WARNUNG: Nicht rückgängig!", "§8⚠ WARNING: Irreversible!"),
    "gui.app.settings.returned_to_server": ("§8Plot geht an Server zurück.", "§8Plot returns to server."),
    "gui.app.settings.abandon_button": ("§c🗑 Plot aufgeben", "§c🗑 Abandon Plot"),
    "gui.app.settings.confirm_abandon": ("⚠ WARNUNG", "⚠ WARNING"),
    "gui.app.settings.confirm_message": ("Plot wirklich aufgeben?\nDiese Aktion kann NICHT\nrückgängig gemacht werden!", "Really abandon plot?\nThis action CANNOT\nbe undone!"),
    "gui.app.settings.returned_warning": ("Plot wird an Server zurückgegeben", "Plot will be returned to server"),
    "gui.app.settings.utility_warnings": ("§e§l⚠ UTILITY-WARNUNGEN", "§e§l⚠ UTILITY WARNINGS"),
    "gui.app.settings.utility_warnings_enabled": (" §fUtility-Warnungen", " §fUtility Warnings"),
    "gui.app.settings.warnings_info1": ("§8Du erhältst Warnungen bei", "§8You receive warnings for"),
    "gui.app.settings.warnings_info2": ("§8hohem Strom-/Wasserverbrauch.", "§8high power/water consumption."),
    "gui.app.settings.thresholds": ("§b§l📊 SCHWELLENWERTE", "§b§l📊 THRESHOLDS"),
    "gui.app.settings.power_warning": ("§e⚡ Strom-Warnung ab:", "§e⚡ Power warning at:"),
    "gui.app.settings.kwh": (" kWh", " kWh"),
    "gui.app.settings.water_warning": ("§b💧 Wasser-Warnung ab:", "§b💧 Water warning at:"),
    "gui.app.settings.liters": (" L", " L"),
    "gui.app.settings.threshold_info1": ("§8Bei Überschreitung siehst du", "§8When exceeded you see"),
    "gui.app.settings.threshold_info2": ("§8eine Warnung in der Finanz-App.", "§8a warning in the Finance app."),
    "gui.app.settings.police_heat": ("§c§l🚨 POLIZEI-HEAT", "§c§l🚨 POLICE HEAT"),
    "gui.app.settings.heat_info1": ("§8Hoher Verbrauch kann", "§8High consumption can"),
    "gui.app.settings.heat_info2": ("§8Aufmerksamkeit erregen!", "§8attract attention!"),
    "gui.app.settings.heat_threshold": ("§c>200 kWh §8oder §c>1000 L", "§c>200 kWh §8or §c>1000 L"),
    "gui.app.settings.balance": ("§6§l💰 KONTOSTAND", "§6§l💰 BALANCE"),
    "gui.app.settings.available": ("§fVerfügbar:", "§fAvailable:"),
    "gui.app.settings.bank_name": ("§8Bank of Schedule", "§8Bank of Schedule"),
    "gui.app.settings.running_costs": ("§e§l📊 LAUFENDE KOSTEN", "§e§l📊 RUNNING COSTS"),
    "gui.app.settings.daily": ("§7Täglich:", "§7Daily:"),
    "gui.app.settings.weekly": ("§7Wöchentlich:", "§7Weekly:"),
    "gui.app.settings.monthly": ("§7Monatlich (30d):", "§7Monthly (30d):"),
    "gui.app.settings.range": ("§8Reichweite: ", "§8Range: "),
    "gui.app.settings.days": (" Tage", " days"),
    "gui.app.settings.property": ("§b§l🏠 EIGENTUM", "§b§l🏠 PROPERTY"),
    "gui.app.settings.no_properties": ("§8Keine Grundstücke", "§8No properties"),
    "gui.app.settings.properties_count": (" Grundstück(e)", " propert(y/ies)"),
    "gui.app.settings.earn_money": ("§8Geld verdienen:", "§8Earn money:"),
    "gui.app.settings.sell_products": ("§7Produkte verkaufen!", "§7Sell products!"),

    # ========================================
    # ContactsAppScreen.java - Kontakte App
    # ========================================
    "gui.app.contacts.title": ("§6§lKontakte", "§6§lContacts"),
    "gui.app.contacts.my_contacts": ("§7Meine Kontakte", "§7My Contacts"),
    "gui.app.contacts.saved": ("§8Gespeicherte Kontakte:", "§8Saved contacts:"),
    "gui.app.contacts.contact1": ("§7• Kontakt 1", "§7• Contact 1"),
    "gui.app.contacts.contact2": ("§7• Kontakt 2", "§7• Contact 2"),

    # ========================================
    # MessagesAppScreen.java - Nachrichten App
    # ========================================
    "gui.app.messages.no_chats": ("§8Keine Chats", "§8No chats"),
    "gui.app.messages.tap_to_chat": ("§7Tippe einen Spieler an,", "§7Tap a player"),
    "gui.app.messages.to_chat": ("§7um zu chatten", "§7to chat"),

    # ========================================
    # AchievementAppScreen.java - Erfolge App
    # ========================================
    "gui.app.achievement.back": ("← Zurück", "← Back"),
    "gui.app.achievement.overview": ("← Übersicht", "← Overview"),
    "gui.app.achievement.title": ("§e§l🏆 Achievements", "§e§l🏆 Achievements"),
    "gui.app.achievement.total_progress": ("§f§lGesamt-Fortschritt", "§f§lTotal Progress"),
    "gui.app.achievement.earned": ("§7Verdient: §a", "§7Earned: §a"),
    "gui.app.achievement.categories": ("§6Kategorien:", "§6Categories:"),
    "gui.app.achievement.no_achievements": ("§7Keine Achievements in dieser Kategorie", "§7No achievements in this category"),
    "gui.app.achievement.unlocked": ("§a§l✓ FREIGESCHALTET", "§a§l✓ UNLOCKED"),
    "gui.app.achievement.in_progress": ("§7○ In Arbeit", "§7○ In Progress"),
    "gui.app.achievement.reward": ("§7Belohnung:", "§7Reward:"),
    "gui.app.achievement.difficulty": ("§7Schwierigkeit: ", "§7Difficulty: "),

    # ========================================
    # ProductsAppScreen.java - Produkte App
    # ========================================
    "gui.app.products.title": ("§6§lProdukte", "§6§lProducts"),
    "gui.app.products.catalog": ("§7Produktkatalog", "§7Product Catalog"),
    "gui.app.products.available": ("§8Verfügbare Produkte:", "§8Available products:"),
    "gui.app.products.product_a": ("§7• Produkt A - 10€", "§7• Product A - 10€"),
    "gui.app.products.product_b": ("§7• Produkt B - 25€", "§7• Product B - 25€"),

    # ========================================
    # OrderAppScreen.java - Bestellungen App
    # ========================================
    "gui.app.order.title": ("§6§lBestellung", "§6§lOrder"),
    "gui.app.order.my_orders": ("§7Meine Bestellungen", "§7My Orders"),
    "gui.app.order.active_orders": ("§8Aktive Bestellungen:", "§8Active orders:"),
    "gui.app.order.no_orders": ("§7Keine Bestellungen", "§7No orders"),

    # ========================================
    # DealerAppScreen.java - Händler App
    # ========================================
    "gui.app.dealer.title": ("§6§lDealer", "§6§lDealer"),
    "gui.app.dealer.overview": ("§7Händler-Übersicht", "§7Dealer Overview"),
    "gui.app.dealer.available": ("§8Verfügbare Händler:", "§8Available dealers:"),
    "gui.app.dealer.dealer1": ("§7• Händler 1", "§7• Dealer 1"),
    "gui.app.dealer.dealer2": ("§7• Händler 2", "§7• Dealer 2"),

    # ========================================
    # PlotInfoHudOverlay.java - Plot-Info HUD
    # ========================================
    "hud.plot.owner": ("§7Besitzer: §cKein Besitzer", "§7Owner: §cNo owner"),
    "hud.plot.owner_name": ("§7Besitzer: §f", "§7Owner: §f"),
    "hud.plot.size": ("§7Größe: §e", "§7Size: §e"),
    "hud.plot.blocks": (" Blöcke", " blocks"),
    "hud.plot.for_sale": ("§a§l⚡ ZUM VERKAUF", "§a§l⚡ FOR SALE"),
    "hud.plot.price": ("§7Preis: §e", "§7Price: §e"),
    "hud.plot.click_for_details": ("§8Rechtsklick für Details", "§8Right-click for details"),
    "hud.plot.rented": ("§a§l✓ VERMIETET", "§a§l✓ RENTED"),
    "hud.plot.days_left": ("§7Noch §e", "§7"),
    "hud.plot.days": (" Tage", " days left"),
    "hud.plot.for_rent": ("§d§l⚡ ZU VERMIETEN", "§d§l⚡ FOR RENT"),
    "hud.plot.rent_price": ("§7Miete: §e", "§7Rent: §e"),
    "hud.plot.apartments": ("§6🏠 Apartments: §e", "§6🏠 Apartments: §e"),
    "hud.plot.available": (" §7verfügbar §8(", " §7available §8("),
    "hud.plot.total": (" gesamt)", " total)"),
    "hud.plot.and_more": ("  §7└─ §8... und ", "  §7└─ §8... and "),
    "hud.plot.more": (" weitere", " more"),
    "hud.plot.per_month": ("€/Monat", "€/month"),
    "hud.plot.click_options": ("§8§oRechtsklick für Details & Optionen", "§8§oRight-click for details & options"),

    # ========================================
    # SmartphoneScreen.java - Smartphone
    # ========================================
    "gui.smartphone.title": ("§6§lSmartphone", "§6§lSmartphone"),
    "gui.smartphone.app.map": ("Map", "Map"),
    "gui.smartphone.app.dealer": ("Dealer", "Dealer"),
    "gui.smartphone.app.products": ("Produkte", "Products"),
    "gui.smartphone.app.order": ("Bestellung", "Order"),
    "gui.smartphone.app.contacts": ("Kontakte", "Contacts"),
    "gui.smartphone.app.messages": ("Nachrichten", "Messages"),
    "gui.smartphone.app.plot": ("Immobilien", "Real Estate"),
    "gui.smartphone.app.settings": ("Settings", "Settings"),
    "gui.smartphone.app.bank": ("Bank", "Bank"),
    "gui.smartphone.app.police": ("Polizei", "Police"),
    "gui.smartphone.app.achievements": ("Erfolge", "Achievements"),

    # ========================================
    # BankerScreen.java - Bank Interface
    # ========================================
    "gui.bank.banker": ("BANKER", "BANKER"),
    "gui.bank.tab_history": ("Historie", "History"),
    "gui.bank.overview_title": ("KONTEN-ÜBERSICHT", "ACCOUNT OVERVIEW"),
    "gui.bank.cash": ("BARGELD", "CASH"),
    "gui.bank.checking_title": ("GIROKONTO", "CHECKING ACCOUNT"),
    "gui.bank.savings_title": ("SPARKONTO", "SAVINGS ACCOUNT"),
    "gui.bank.total": ("GESAMT:", "TOTAL:"),
    "gui.bank.balance": ("Kontostand:", "Balance:"),
    "gui.bank.cash_balance": ("Bargeld:", "Cash:"),
    "gui.bank.checking_balance": ("Girokonto:", "Checking:"),
    "gui.bank.deposit_from_cash": ("Einzahlen (aus Bargeld):", "Deposit (from cash):"),
    "gui.bank.withdraw_to_cash": ("Abheben (zu Bargeld):", "Withdraw (to cash):"),
    "gui.bank.limit": ("Limit: ", "Limit: "),
    "gui.bank.deposit_from_checking": ("Einzahlen (vom Girokonto):", "Deposit (from checking):"),
    "gui.bank.withdraw_to_checking": ("Abheben (zum Girokonto):", "Withdraw (to checking):"),
    "gui.bank.interest": ("Zinsen: 5% pro Woche", "Interest: 5% per week"),
    "gui.bank.minimum": ("Minimum: 1000€", "Minimum: 1000€"),
    "gui.bank.lock_period": ("4-Wochen Sperre", "4-week lock"),
    "gui.bank.transfer_title": ("ÜBERWEISUNG", "TRANSFER"),
    "gui.bank.recipient_name": ("Empfängername:", "Recipient name:"),
    "gui.bank.amount_label": ("Betrag in €:", "Amount in €:"),
    "gui.bank.available_balance": ("Verfügbar:", "Available:"),
    "gui.bank.daily_limit": ("Tageslimit:", "Daily limit:"),
    "gui.bank.history_title": ("TRANSAKTIONEN", "TRANSACTIONS"),
    "gui.bank.no_transactions": ("Keine Transaktionen", "No transactions"),
    "gui.bank.standing_orders_title": ("DAUERAUFTRÄGE", "STANDING ORDERS"),
    "gui.bank.new_standing_order": ("Neuer Dauerauftrag:", "New standing order:"),
    "gui.bank.recipient": ("Empfänger:", "Recipient:"),
    "gui.bank.interval": ("Intervall:", "Interval:"),
    "gui.bank.create": ("Erstellen", "Create"),
    "gui.bank.active_orders": ("Aktive Daueraufträge:", "Active standing orders:"),
    "gui.bank.no_standing_orders": ("Keine aktiven Daueraufträge", "No active standing orders"),
    "gui.bank.credit_payment": ("💳 KREDIT", "💳 CREDIT"),
    "gui.bank.interval_daily": ("Täglich", "Daily"),

    # ========================================
    # RecurringPaymentInterval.java
    # ========================================
    "interval.daily": ("Täglich", "Daily"),
    "interval.weekly": ("Wöchentlich", "Weekly"),
    "interval.monthly": ("Monatlich", "Monthly"),

    # ========================================
    # WantedLevelOverlay.java - Wanted System
    # ========================================
    "hud.wanted.wanted": ("§cGesucht: ", "§cWanted: "),
    "hud.wanted.hidden": ("§eVersteckt: §f", "§eHidden: §f"),

    # ========================================
    # PlotMenuGUI.java - Plot Menu
    # ========================================
    "gui.plot.owned_plots": ("§a§lEigene Plots", "§a§lOwned Plots"),
    "gui.plot.manage_plots": ("§7Verwalte deine Plots", "§7Manage your plots"),
    "gui.plot.plots_count": ("§7Plots: §e", "§7Plots: §e"),
    "gui.plot.click_to_open": ("§eKlick zum Öffnen", "§eClick to open"),
    "gui.plot.buy_plots": ("§a§lPlots kaufen", "§a§lBuy Plots"),
    "gui.plot.buy_available": ("§7Kaufe verfügbare Plots", "§7Buy available plots"),
    "gui.plot.available_count": ("§7Verfügbar: §e", "§7Available: §e"),
    "gui.plot.rent_plots": ("§d§lPlots mieten", "§d§lRent Plots"),
    "gui.plot.rent_temporary": ("§7Miete Plots temporär", "§7Rent plots temporarily"),
    "gui.plot.to_rent_count": ("§7Zur Miete: §e", "§7To rent: §e"),
    "gui.plot.top_plots": ("§6§lTop Plots", "§6§lTop Plots"),
    "gui.plot.top_rated": ("§7Bestbewertete Plots", "§7Top rated plots"),
    "gui.plot.top_10": ("§7Top 10 nach Rating", "§7Top 10 by rating"),
    "gui.plot.shop": ("§e§lShop", "§e§lShop"),
    "gui.plot.buy_sell_items": ("§7Items kaufen & verkaufen", "§7Buy & sell items"),
    "gui.plot.daily_reward_info": ("§7Hole deine tägliche Belohnung ab!", "§7Collect your daily reward!"),
    "gui.plot.daily_command": ("§7Befehl: §e/daily", "§7Command: §e/daily"),
    "gui.plot.build_streak": ("§7Baue einen Streak auf für Boni!", "§7Build a streak for bonuses!"),
    "gui.plot.statistics": ("§b§lStatistiken", "§b§lStatistics"),
    "gui.plot.your_stats": ("§7Deine Plot-Statistiken", "§7Your plot statistics"),
    "gui.plot.owned": ("§7Besessen: §e", "§7Owned: §e"),
    "gui.plot.rented": ("§7Gemietet: §e", "§7Rented: §e"),
    "gui.plot.trusted_in": ("§7Vertraut in: §e", "§7Trusted in: §e"),

    # ========================================
    # PlotInfoScreen.java - Plot Info Screen
    # ========================================
    "gui.plotinfo.buy_button": ("§a§lKaufen", "§a§lBuy"),
    "gui.plotinfo.rent_button": ("§d§lMieten", "§d§lRent"),
    "gui.plotinfo.apartment_rent": ("Mieten", "Rent"),
    "gui.plotinfo.owner": ("§7Besitzer: §cKein Besitzer", "§7Owner: §cNo owner"),
    "gui.plotinfo.owner_name": ("§7Besitzer: §f", "§7Owner: §f"),
    "gui.plotinfo.size": ("§7Größe: §e", "§7Size: §e"),
    "gui.plotinfo.blocks": (" Blöcke", " blocks"),
    "gui.plotinfo.id": ("§8ID: ", "§8ID: "),
    "gui.plotinfo.rating_title": ("§6§l⭐ BEWERTUNG", "§6§l⭐ RATING"),
    "gui.plotinfo.rating_average": ("§7Durchschnitt: §e", "§7Average: §e"),
    "gui.plotinfo.ratings_count": (" Bewertungen)", " ratings)"),
    "gui.plotinfo.no_ratings": ("§7Noch keine Bewertungen", "§7No ratings yet"),
    "gui.plotinfo.your_rating": ("§7Deine Bewertung: §e", "§7Your rating: §e"),
    "gui.plotinfo.click_to_rate": ("§7Klicke auf Sterne zum Bewerten:", "§7Click stars to rate:"),
    "gui.plotinfo.for_sale": ("§a§l⚡ ZUM VERKAUF", "§a§l⚡ FOR SALE"),
    "gui.plotinfo.price": ("§7Preis: §e", "§7Price: §e"),
    "gui.plotinfo.rented": ("§a§l✓ VERMIETET", "§a§l✓ RENTED"),
    "gui.plotinfo.days_left": ("§7Noch §e", "§7"),
    "gui.plotinfo.days": (" Tage", " days left"),
    "gui.plotinfo.for_rent": ("§d§l⚡ ZU VERMIETEN", "§d§l⚡ FOR RENT"),
    "gui.plotinfo.rent_price": ("§7Miete: §e", "§7Rent: §e"),
    "gui.plotinfo.apartments_title": ("§6§l🏠 APARTMENTS", "§6§l🏠 APARTMENTS"),
    "gui.plotinfo.apartments_total": ("§7Gesamt: §e", "§7Total: §e"),
    "gui.plotinfo.apartments_available": ("§7Verfügbar: §a", "§7Available: §a"),
    "gui.plotinfo.apartments_rented": ("§7Vermietet: §c", "§7Rented: §c"),
    "gui.plotinfo.available_apartments": ("§d§lVERFÜGBARE WOHNUNGEN:", "§d§lAVAILABLE APARTMENTS:"),
    "gui.plotinfo.per_month": ("€/Monat", "€/month"),

    # ========================================
    # WarehouseScreen.java - Warehouse
    # ========================================
    "gui.warehouse.tab_items": ("Items", "Items"),
    "gui.warehouse.tab_sellers": ("Seller", "Sellers"),
    "gui.warehouse.tab_stats": ("Stats", "Stats"),
    "gui.warehouse.tab_settings": ("Einstellungen", "Settings"),
    "gui.warehouse.item_list": ("§lITEM LISTE", "§lITEM LIST"),
    "gui.warehouse.empty": ("Leer", "Empty"),
    "gui.warehouse.slot_details": ("§lSLOT DETAILS", "§lSLOT DETAILS"),
    "gui.warehouse.item": ("Item: ", "Item: "),
    "gui.warehouse.stock": ("Bestand: ", "Stock: "),
    "gui.warehouse.max": ("Max: ", "Max: "),
    "gui.warehouse.free": ("Frei: ", "Free: "),
    "gui.warehouse.no_slot_selected": ("Kein Slot ausgewählt", "No slot selected"),
    "gui.warehouse.how_to_add": ("§7So fügen Sie Items hinzu:", "§7How to add items:"),
    "gui.warehouse.use_command": ("§7Verwenden Sie den Command:", "§7Use the command:"),
    "gui.warehouse.command_example": ("§e/warehouse add <item> <amount>", "§e/warehouse add <item> <amount>"),
    "gui.warehouse.npc_shop": ("§l§eNPC SHOP", "§l§eNPC SHOP"),
    "gui.warehouse.no_npc_shop": ("§7Kein NPC Shop", "§7No NPC shop"),
    "gui.warehouse.more": ("§7+", "§7+"),
    "gui.warehouse.more_items": (" mehr...", " more..."),
    "gui.warehouse.slots_usage": ("Slots: ", "Slots: "),
    "gui.warehouse.linked_sellers": ("§lVERKNÜPFTE VERKÄUFER", "§lLINKED SELLERS"),
    "gui.warehouse.no_sellers_linked": ("Keine Verkäufer verknüpft", "No sellers linked"),
    "gui.warehouse.available_npcs": ("§lVERFÜGBARE NPCS", "§lAVAILABLE NPCS"),
    "gui.warehouse.all_npcs_linked": ("Alle NPCs sind verknüpft", "All NPCs are linked"),
    "gui.warehouse.linked": ("Verknüpft: ", "Linked: "),
    "gui.warehouse.available": ("Verfügbar: ", "Available: "),
    "gui.warehouse.stats_title": ("§l§e📊 LAGERBESTAND ÜBERSICHT", "§l§e📊 INVENTORY OVERVIEW"),
    "gui.warehouse.usage_percent": (" ausgelastet (", " utilized ("),
    "gui.warehouse.top_5_items": ("Top 5 Items nach Bestand:", "Top 5 items by stock:"),
    "gui.warehouse.full": (" [VOLL]", " [FULL]"),
    "gui.warehouse.finances_title": ("§l§e💰 FINANZEN", "§l§e💰 FINANCES"),
    "gui.warehouse.net_revenue_7days": ("Nettoumsatz (7 Tage): ", "Net revenue (7 days): "),
    "gui.warehouse.expenses_30days": ("Ausgaben (30 Tage): ", "Expenses (30 days): "),
    "gui.warehouse.deliveries": ("  Lieferungen: ", "  Deliveries: "),
    "gui.warehouse.avg_delivery": ("x | Ø ", "x | Avg "),
    "gui.warehouse.recent_deliveries": ("  Letzte Lieferungen:", "  Recent deliveries:"),
    "gui.warehouse.today": ("heute", "today"),
    "gui.warehouse.days_ago": ("vor ", ""),
    "gui.warehouse.days": ("d", "d ago"),
    "gui.warehouse.shop_account_not_found": ("Shop-Konto nicht gefunden: ", "Shop account not found: "),
    "gui.warehouse.no_shop_account": ("Kein Shop-Konto verknüpft", "No shop account linked"),
    "gui.warehouse.auto_delivery_title": ("§l§e📦 AUTO-DELIVERY", "§l§e📦 AUTO-DELIVERY"),
    "gui.warehouse.status_active": ("Status: Aktiv ✓", "Status: Active ✓"),
    "gui.warehouse.next_delivery": ("Nächste Lieferung: in ", "Next delivery: in "),
    "gui.warehouse.delivery_days": (" Tagen", " days"),
    "gui.warehouse.interval_days": ("Interval: alle ", "Interval: every "),
    "gui.warehouse.days_interval": (" Tage", " days"),
    "gui.warehouse.config_title": ("§l§e⚙ WAREHOUSE KONFIGURATION", "§l§e⚙ WAREHOUSE CONFIGURATION"),
    "gui.warehouse.auto_delivery_label": ("Auto-Delivery:", "Auto-Delivery:"),
    "gui.warehouse.enabled_yes": ("  Aktiviert: Ja", "  Enabled: Yes"),
    "gui.warehouse.interval_label": ("  Interval: ", "  Interval: "),
    "gui.warehouse.slot_config": ("Slot-Konfiguration:", "Slot Configuration:"),
    "gui.warehouse.slot_count": ("  Anzahl Slots: ", "  Slot count: "),
    "gui.warehouse.max_capacity": ("  Max Kapazität/Slot: ", "  Max capacity/slot: "),
    "gui.warehouse.permissions": ("Berechtigungen:", "Permissions:"),
    "gui.warehouse.admin_only": ("  ✓ Nur Admin kann bearbeiten", "  ✓ Admin only can edit"),
    "gui.warehouse.sellers_can_view": ("  ✓ Seller können Bestand sehen", "  ✓ Sellers can view stock"),
    "gui.warehouse.item_select_title": ("§lItem auswählen", "§lSelect Item"),
    "gui.warehouse.search_label": ("Suche:", "Search:"),
    "gui.warehouse.cancel": ("Abbrechen", "Cancel"),
    "gui.warehouse.items_count": (" Items", " Items"),
    "gui.warehouse.error_no_seller": ("§cFehler: Es muss mindestens ein Verkäufer-NPC verknüpft sein, bevor Items hinzugefügt werden können!", "§cError: At least one seller NPC must be linked before items can be added!"),

    # ========================================
    # Common GUI Elements (already internationalized but listed for completeness)
    # ========================================
    "gui.common.back": ("Zurück", "Back"),
    "gui.common.close": ("Schließen", "Close"),
    "gui.common.close_red": ("§cSchließen", "§cClose"),
    "gui.common.save": ("Speichern", "Save"),
    "gui.common.cancel": ("Abbrechen", "Cancel"),
    "gui.common.confirm": ("Bestätigen", "Confirm"),
    "gui.common.amount": ("Betrag", "Amount"),
    "gui.common.player_name": ("Spielername", "Player Name"),
    "gui.common.search": ("Suche", "Search"),
    "gui.common.clear": ("Leeren", "Clear"),
    "gui.confirm_dialog.yes": ("Ja", "Yes"),
    "gui.confirm_dialog.cancel": ("Nein", "No"),
    "gui.input_dialog.confirm": ("Bestätigen", "Confirm"),
    "gui.input_dialog.cancel": ("Abbrechen", "Cancel"),
    "gui.daily_reward": ("Tägliche Belohnung", "Daily Reward"),

    # ========================================
    # Messages (already mostly internationalized, completing the list)
    # ========================================
    "message.bank.recipient": ("Empfänger", "Recipient"),
    "message.warehouse.settings_saved": ("Einstellungen gespeichert!", "Settings saved!"),
}

def load_json(file_path):
    """Load existing JSON file or return empty dict if not exists"""
    if os.path.exists(file_path):
        with open(file_path, 'r', encoding='utf-8') as f:
            return json.load(f)
    return {}

def save_json(file_path, data):
    """Save JSON file with proper formatting"""
    with open(file_path, 'w', encoding='utf-8') as f:
        json.dump(data, f, ensure_ascii=False, indent=2, sort_keys=True)

def main():
    print("=" * 60)
    print("ScheduleMC - Translation Key Addition Script")
    print("=" * 60)
    print()

    # Load existing translations
    print(f"Loading existing translations from {DE_FILE}...")
    de_translations = load_json(DE_FILE)
    print(f"  Found {len(de_translations)} existing German keys")

    print(f"Loading existing translations from {EN_FILE}...")
    en_translations = load_json(EN_FILE)
    print(f"  Found {len(en_translations)} existing English keys")
    print()

    # Count new translations
    new_keys = 0
    updated_keys = 0

    print("Adding new translation keys...")
    for key, (de_text, en_text) in TRANSLATIONS.items():
        # German
        if key not in de_translations:
            de_translations[key] = de_text
            new_keys += 1
        elif de_translations[key] != de_text:
            de_translations[key] = de_text
            updated_keys += 1

        # English
        if key not in en_translations:
            en_translations[key] = en_text
        elif en_translations[key] != en_text:
            en_translations[key] = en_text

    # Save updated translations
    print(f"\nSaving updated translations to {DE_FILE}...")
    save_json(DE_FILE, de_translations)
    print(f"  Saved {len(de_translations)} German keys")

    print(f"Saving updated translations to {EN_FILE}...")
    save_json(EN_FILE, en_translations)
    print(f"  Saved {len(en_translations)} English keys")

    # Summary
    print()
    print("=" * 60)
    print("SUMMARY")
    print("=" * 60)
    print(f"Total translation keys processed: {len(TRANSLATIONS)}")
    print(f"New keys added: {new_keys}")
    print(f"Existing keys updated: {updated_keys}")
    print(f"Unchanged keys: {len(TRANSLATIONS) - new_keys - updated_keys}")
    print()
    print("Translation files updated successfully!")
    print()
    print("Files affected:")
    print(f"  - PlotAppScreen.java (56+ strings)")
    print(f"  - SettingsAppScreen.java (63+ strings)")
    print(f"  - ContactsAppScreen.java (6 strings)")
    print(f"  - MessagesAppScreen.java (4 strings)")
    print(f"  - AchievementAppScreen.java (19 strings)")
    print(f"  - ProductsAppScreen.java (6 strings)")
    print(f"  - OrderAppScreen.java (5 strings)")
    print(f"  - DealerAppScreen.java (6 strings)")
    print(f"  - PlotInfoHudOverlay.java (11+ strings)")
    print(f"  - SmartphoneScreen.java (11 strings)")
    print(f"  - BankerScreen.java (40+ strings)")
    print(f"  - WantedLevelOverlay.java (2 strings)")
    print(f"  - PlotMenuGUI.java (27 strings)")
    print(f"  - PlotInfoScreen.java (32 strings)")
    print(f"  - WarehouseScreen.java (60+ strings)")
    print(f"  - RecurringPaymentInterval.java (3 strings)")
    print(f"  - ConfirmDialogScreen.java (2 strings)")
    print(f"  - InputDialogScreen.java (2 strings)")
    print()
    print("Total affected files: ~18 Java files")
    print()
    print("Next steps:")
    print("1. Replace hardcoded strings in Java files with Component.translatable() calls")
    print("2. Test all GUIs to ensure translations work correctly")
    print("3. Check for any remaining hardcoded strings")
    print()

if __name__ == "__main__":
    main()

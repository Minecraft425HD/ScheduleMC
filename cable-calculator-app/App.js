import React, { useState } from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  ScrollView,
  StyleSheet,
  StatusBar,
  SafeAreaView,
  KeyboardAvoidingView,
  Platform,
} from 'react-native';

// ─── DIN VDE 0298-4 Berechnungsdaten ─────────────────────────────────────────

const KUPFER_QUERSCHNITTE = [1.5, 2.5, 4, 6, 10, 16, 25, 35, 50, 70, 95, 120];
const ALU_QUERSCHNITTE    = [16, 25, 35, 50, 70, 95, 120];

// Maximale Dauerstrombelastbarkeit [A] nach DIN VDE 0298-4
// Spalten: [0] Unterputz (A2), [1] Aufputz (B2), [2] Im Rohr (B1)
const KUPFER = {
  1.5:  [14.5, 17.5, 15.5],
  2.5:  [19.5, 24.0, 21.0],
  4:    [26.0, 32.0, 28.0],
  6:    [34.0, 41.0, 36.0],
  10:   [46.0, 57.0, 50.0],
  16:   [61.0, 76.0, 66.0],
  25:   [80.0, 101.0, 87.0],
  35:   [99.0, 125.0, 107.0],
  50:   [119.0, 151.0, 129.0],
  70:   [151.0, 192.0, 163.0],
  95:   [182.0, 232.0, 198.0],
  120:  [210.0, 269.0, 229.0],
};

const ALUMINIUM = {
  16:   [49.0,  59.0,  51.0],
  25:   [63.0,  77.0,  68.0],
  35:   [77.0,  96.0,  83.0],
  50:   [94.0,  116.0, 101.0],
  70:   [119.0, 149.0, 128.0],
  95:   [144.0, 180.0, 155.0],
  120:  [168.0, 209.0, 180.0],
};

const VERLEGEART_IDX = { unterputz: 0, aufputz: 1, imRohr: 2 };

// Spezifischer Widerstand [Ω·mm²/m]
const RHO = { kupfer: 0.0178, aluminium: 0.0282 };

const U_N = 230; // Nennspannung [V]

// ─── Berechnungslogik ─────────────────────────────────────────────────────────

function berechneQuerschnitt(strom, laenge, material, verlegeart) {
  const tabelle      = material === 'kupfer' ? KUPFER : ALUMINIUM;
  const querschnitte = material === 'kupfer' ? KUPFER_QUERSCHNITTE : ALU_QUERSCHNITTE;
  const vi           = VERLEGEART_IDX[verlegeart];
  const rho          = RHO[material];

  // Schritt 1: Mindestquerschnitt nach Strombelastbarkeit
  const mindestIdx = querschnitte.findIndex(q => tabelle[q][vi] >= strom);
  if (mindestIdx === -1) {
    const maxA = tabelle[querschnitte[querschnitte.length - 1]][vi];
    return {
      fehler:
        `${strom} A übersteigt den Maximalwert für ` +
        `${material === 'kupfer' ? 'Kupfer' : 'Aluminium'} bei dieser Verlegeart ` +
        `(${maxA} A bei 120 mm²). Bitte einen Elektriker hinzuziehen.`,
    };
  }

  // Schritt 2: Spannungsfall prüfen – ΔU = 2 × L × I × ρ / A (einphasig, 230 V)
  // Querschnitt ggf. erhöhen bis ΔU ≤ 3 %
  let finalIdx = mindestIdx;
  for (let i = mindestIdx; i < querschnitte.length; i++) {
    finalIdx = i;
    const du = (2 * laenge * strom * rho) / querschnitte[i];
    if ((du / U_N) * 100 <= 3.0) break;
  }

  const q         = querschnitte[finalIdx];
  const mindestQ  = querschnitte[mindestIdx];
  const du        = (2 * laenge * strom * rho) / q;
  const duPct     = (du / U_N) * 100;

  return {
    querschnitt:            q,
    mindestQuerschnitt:     mindestQ,
    maxStrom:               tabelle[q][vi],
    spannungsfall:          du,
    spannungsfallProzent:   duPct,
    spannungsfallWarnung:   duPct > 3.0,
    vergroessert:           finalIdx > mindestIdx,
  };
}

// ─── Ausgabetext ──────────────────────────────────────────────────────────────

const VERLEGEART_LABEL = {
  unterputz: 'Unterputz (Verlegeart A2)',
  aufputz:   'Aufputz (Verlegeart B2)',
  imRohr:    'Im Rohr (Verlegeart B1)',
};

function erklaerung(r, strom, material, verlegeart) {
  const mat = material === 'kupfer' ? 'Kupfer' : 'Aluminium';
  const vl  = VERLEGEART_LABEL[verlegeart];
  const duStr  = r.spannungsfall.toFixed(2);
  const pctStr = r.spannungsfallProzent.toFixed(1);

  if (r.vergroessert) {
    return (
      `Nach Strombelastbarkeit wäre ${r.mindestQuerschnitt} mm² ` +
      `ausreichend für ${strom} A ${mat} bei ${vl}.\n\n` +
      `Wegen des Spannungsfalls (${duStr} V = ${pctStr} %) wurde der ` +
      `Querschnitt auf ${r.querschnitt} mm² erhöht, um den 3-%-Grenzwert ` +
      `nach DIN VDE 0100-520 einzuhalten.`
    );
  }
  return (
    `${r.querschnitt} mm² ist ausreichend für ${strom} A ${mat} ` +
    `bei ${vl} (Dauerstrom-Maximum: ${r.maxStrom} A).\n\n` +
    `Spannungsfall: ${duStr} V (${pctStr} %) – ` +
    `innerhalb des 3-%-Grenzwerts nach DIN VDE 0100-520.`
  );
}

// ─── Farben ───────────────────────────────────────────────────────────────────

const C = {
  bg:            '#0D1E35',
  card:          '#132236',
  cardBorder:    '#1E3A5F',
  primary:       '#2563EB',
  primaryPress:  '#1D4ED8',
  text:          '#FFFFFF',
  textMuted:     '#94B4D4',
  inputBg:       '#0F1E33',
  inputBorder:   '#2A4A70',
  toggleActiveBg:'#2563EB',
  toggleBorder:  '#60A5FA',
  warning:       '#F97316',
  warningBg:     '#2C1506',
  warningBorder: '#7C3009',
  warnText:      '#FED7AA',
  errorBg:       '#1A0505',
  errorBorder:   '#7F1D1D',
  errorText:     '#FCA5A5',
  errorTitle:    '#EF4444',
  divider:       '#1E3A5F',
  footerText:    '#5E82A8',
};

// ─── Styles ───────────────────────────────────────────────────────────────────

const s = StyleSheet.create({
  safe:    { flex: 1, backgroundColor: C.bg },
  kav:     { flex: 1 },
  scroll:  { flex: 1 },
  content: { paddingHorizontal: 18, paddingBottom: 52 },

  // Header
  header:   { alignItems: 'center', paddingTop: 28, paddingBottom: 26 },
  title:    { fontSize: 22, fontWeight: '700', color: C.text, letterSpacing: 0.2 },
  subtitle: { fontSize: 13, color: C.textMuted, marginTop: 5 },

  // Card
  card:  {
    backgroundColor: C.card,
    borderRadius: 14,
    borderWidth: 1,
    borderColor: C.cardBorder,
    padding: 18,
    marginBottom: 14,
  },
  label: {
    fontSize: 12,
    color: C.textMuted,
    fontWeight: '700',
    textTransform: 'uppercase',
    letterSpacing: 1,
    marginBottom: 12,
  },

  // Eingabezeile
  inputRow: { flexDirection: 'row', alignItems: 'center' },
  input: {
    flex: 1,
    backgroundColor: C.inputBg,
    borderRadius: 10,
    borderWidth: 1,
    borderColor: C.inputBorder,
    color: C.text,
    fontSize: 28,
    fontWeight: '600',
    paddingHorizontal: 16,
    paddingVertical: 14,
    marginRight: 12,
  },
  unit: { fontSize: 20, color: C.textMuted, fontWeight: '600', minWidth: 30 },

  // Toggle-Schaltflächen
  toggleRow: { flexDirection: 'row', gap: 8 },
  toggleBtn: {
    flex: 1,
    paddingVertical: 14,
    borderRadius: 10,
    borderWidth: 1,
    borderColor: C.cardBorder,
    alignItems: 'center',
    backgroundColor: C.inputBg,
  },
  toggleBtnOn: {
    backgroundColor: C.toggleActiveBg,
    borderColor: C.toggleBorder,
  },
  toggleTxt:   { fontSize: 15, color: C.textMuted, fontWeight: '500' },
  toggleTxtOn: { color: C.text, fontWeight: '700' },

  // Berechnen-Button
  calcBtn: {
    backgroundColor: C.primary,
    borderRadius: 14,
    paddingVertical: 18,
    alignItems: 'center',
    marginTop: 4,
    marginBottom: 22,
  },
  calcBtnPress: { backgroundColor: C.primaryPress },
  calcBtnTxt:   { fontSize: 18, fontWeight: '700', color: C.text, letterSpacing: 0.4 },

  // Eingabe-Fehler
  inputErrTxt: {
    color: C.errorTitle,
    fontSize: 14,
    marginBottom: 10,
    textAlign: 'center',
  },

  // Ergebnis-Karte
  resultCard: {
    backgroundColor: C.card,
    borderRadius: 14,
    borderWidth: 1,
    borderColor: C.cardBorder,
    padding: 20,
    marginBottom: 14,
  },
  resultTitle:   { fontSize: 12, color: C.textMuted, fontWeight: '700', textTransform: 'uppercase', letterSpacing: 1, marginBottom: 16 },
  resultCenter:  { alignItems: 'center', paddingVertical: 10 },
  resultQ:       { fontSize: 64, fontWeight: '800', color: C.text, letterSpacing: -2 },
  resultQUnit:   { fontSize: 18, color: C.textMuted, marginTop: 2 },

  divider: { height: 1, backgroundColor: C.divider, marginVertical: 18 },

  exTitle: { fontSize: 12, color: C.textMuted, fontWeight: '700', textTransform: 'uppercase', letterSpacing: 1, marginBottom: 10 },
  exText:  { fontSize: 14, color: C.text, lineHeight: 22 },

  // Spannungsfall-Warnung
  warnBox: {
    marginTop: 16,
    backgroundColor: C.warningBg,
    borderRadius: 10,
    borderWidth: 1,
    borderColor: C.warningBorder,
    padding: 14,
  },
  warnTitle: { fontSize: 15, fontWeight: '700', color: C.warning, marginBottom: 6 },
  warnText:  { fontSize: 13, color: C.warnText, lineHeight: 20 },

  // Fehler-Karte (Berechnungsfehler)
  errCard: {
    backgroundColor: C.errorBg,
    borderRadius: 14,
    borderWidth: 1,
    borderColor: C.errorBorder,
    padding: 20,
    marginBottom: 14,
  },
  errCardTitle: { fontSize: 15, fontWeight: '700', color: C.errorTitle, marginBottom: 8 },
  errCardText:  { fontSize: 14, color: C.errorText, lineHeight: 20 },

  // Footer
  footer:    { alignItems: 'center', marginTop: 8 },
  footerTxt: { fontSize: 11, color: C.footerText, textAlign: 'center', lineHeight: 17 },
});

// ─── Wiederverwendbare Komponente: Toggle-Gruppe ──────────────────────────────

function ToggleGroup({ options, value, onChange }) {
  return (
    <View style={s.toggleRow}>
      {options.map(opt => (
        <TouchableOpacity
          key={opt.key}
          style={[s.toggleBtn, value === opt.key && s.toggleBtnOn]}
          onPress={() => onChange(opt.key)}
          activeOpacity={0.75}
        >
          <Text style={[s.toggleTxt, value === opt.key && s.toggleTxtOn]}>
            {opt.label}
          </Text>
        </TouchableOpacity>
      ))}
    </View>
  );
}

// ─── Hauptkomponente ──────────────────────────────────────────────────────────

export default function App() {
  const [strom,      setStrom]      = useState('');
  const [laenge,     setLaenge]     = useState('');
  const [material,   setMaterial]   = useState('kupfer');
  const [verlegeart, setVerlegeart] = useState('unterputz');
  const [ergebnis,   setErgebnis]   = useState(null);
  const [inputFehler, setInputFehler] = useState('');
  const [btnPressed, setBtnPressed] = useState(false);

  const resetErgebnis = () => setErgebnis(null);

  const handleMaterial = val => { setMaterial(val); resetErgebnis(); };
  const handleVerlegeart = val => { setVerlegeart(val); resetErgebnis(); };

  const handleBerechnen = () => {
    const s_val = parseFloat(strom.replace(',', '.'));
    const l_val = parseFloat(laenge.replace(',', '.'));

    if (!strom.trim() || isNaN(s_val) || s_val <= 0) {
      setInputFehler('Bitte eine gültige Stromstärke eingeben (> 0 A).');
      setErgebnis(null);
      return;
    }
    if (!laenge.trim() || isNaN(l_val) || l_val <= 0) {
      setInputFehler('Bitte eine gültige Leitungslänge eingeben (> 0 m).');
      setErgebnis(null);
      return;
    }

    setInputFehler('');
    setErgebnis(berechneQuerschnitt(s_val, l_val, material, verlegeart));
  };

  const stromZahl  = parseFloat(strom.replace(',', '.'));

  return (
    <SafeAreaView style={s.safe}>
      <StatusBar barStyle="light-content" backgroundColor={C.bg} />
      <KeyboardAvoidingView
        style={s.kav}
        behavior={Platform.OS === 'ios' ? 'padding' : undefined}
      >
        <ScrollView
          style={s.scroll}
          contentContainerStyle={s.content}
          keyboardShouldPersistTaps="handled"
        >
          {/* ── Header ────────────────────────────────────────── */}
          <View style={s.header}>
            <Text style={s.title}>Kabelquerschnitt-Rechner</Text>
            <Text style={s.subtitle}>nach DIN VDE 0298</Text>
          </View>

          {/* ── Stromstärke ───────────────────────────────────── */}
          <View style={s.card}>
            <Text style={s.label}>Stromstärke</Text>
            <View style={s.inputRow}>
              <TextInput
                style={s.input}
                value={strom}
                onChangeText={t => { setStrom(t); resetErgebnis(); }}
                keyboardType="decimal-pad"
                placeholder="0"
                placeholderTextColor={C.textMuted}
                returnKeyType="next"
                maxLength={8}
              />
              <Text style={s.unit}>A</Text>
            </View>
          </View>

          {/* ── Leitungslänge ─────────────────────────────────── */}
          <View style={s.card}>
            <Text style={s.label}>Leitungslänge</Text>
            <View style={s.inputRow}>
              <TextInput
                style={s.input}
                value={laenge}
                onChangeText={t => { setLaenge(t); resetErgebnis(); }}
                keyboardType="decimal-pad"
                placeholder="0"
                placeholderTextColor={C.textMuted}
                returnKeyType="done"
                maxLength={8}
              />
              <Text style={s.unit}>m</Text>
            </View>
          </View>

          {/* ── Material ──────────────────────────────────────── */}
          <View style={s.card}>
            <Text style={s.label}>Material</Text>
            <ToggleGroup
              options={[
                { key: 'kupfer',    label: 'Kupfer' },
                { key: 'aluminium', label: 'Aluminium' },
              ]}
              value={material}
              onChange={handleMaterial}
            />
          </View>

          {/* ── Verlegeart ────────────────────────────────────── */}
          <View style={s.card}>
            <Text style={s.label}>Verlegeart</Text>
            <ToggleGroup
              options={[
                { key: 'unterputz', label: 'Unterputz' },
                { key: 'aufputz',   label: 'Aufputz' },
                { key: 'imRohr',    label: 'Im Rohr' },
              ]}
              value={verlegeart}
              onChange={handleVerlegeart}
            />
          </View>

          {/* ── Eingabe-Fehler ────────────────────────────────── */}
          {!!inputFehler && (
            <Text style={s.inputErrTxt}>{inputFehler}</Text>
          )}

          {/* ── Berechnen-Schaltfläche ────────────────────────── */}
          <TouchableOpacity
            style={[s.calcBtn, btnPressed && s.calcBtnPress]}
            onPress={handleBerechnen}
            onPressIn={() => setBtnPressed(true)}
            onPressOut={() => setBtnPressed(false)}
            activeOpacity={0.85}
          >
            <Text style={s.calcBtnTxt}>Berechnen</Text>
          </TouchableOpacity>

          {/* ── Ergebnis-Karte ───────────────────────────────── */}
          {ergebnis && !ergebnis.fehler && (
            <View style={s.resultCard}>
              <Text style={s.resultTitle}>Ergebnis</Text>

              <View style={s.resultCenter}>
                <Text style={s.resultQ}>{ergebnis.querschnitt}</Text>
                <Text style={s.resultQUnit}>mm²</Text>
              </View>

              <View style={s.divider} />

              <Text style={s.exTitle}>Begründung</Text>
              <Text style={s.exText}>
                {erklaerung(ergebnis, stromZahl, material, verlegeart)}
              </Text>

              {ergebnis.spannungsfallWarnung && (
                <View style={s.warnBox}>
                  <Text style={s.warnTitle}>⚠ Spannungsfall über 3 %</Text>
                  <Text style={s.warnText}>
                    Der Spannungsfall beträgt{' '}
                    {ergebnis.spannungsfallProzent.toFixed(1)} % und
                    überschreitet den nach DIN VDE 0100-520 empfohlenen
                    Grenzwert von 3 %. Bitte einen größeren Querschnitt wählen
                    oder die Leitungslänge reduzieren. Rücksprache mit einem
                    Elektrofachbetrieb empfohlen.
                  </Text>
                </View>
              )}
            </View>
          )}

          {/* ── Fehler-Karte (Berechnungsfehler) ─────────────── */}
          {ergebnis && ergebnis.fehler && (
            <View style={s.errCard}>
              <Text style={s.errCardTitle}>Eingabe außerhalb des Bereichs</Text>
              <Text style={s.errCardText}>{ergebnis.fehler}</Text>
            </View>
          )}

          {/* ── Footer ───────────────────────────────────────── */}
          <View style={s.footer}>
            <Text style={s.footerTxt}>
              Richtwerte nach DIN VDE 0298-4 · Einphasig 230 V~{'\n'}
              Keine Gewähr – Installationen nur durch Elektrofachkraft
            </Text>
          </View>
        </ScrollView>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}

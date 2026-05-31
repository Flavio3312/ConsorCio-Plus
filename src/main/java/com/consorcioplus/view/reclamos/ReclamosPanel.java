package com.consorcioplus.view.reclamos;

import com.consorcioplus.controller.ConsorcioController;
import com.consorcioplus.controller.ReclamoController;
import com.consorcioplus.controller.UnidadFuncionalController;
import com.consorcioplus.model.entity.Consorcio;
import com.consorcioplus.model.entity.Reclamo;
import com.consorcioplus.model.entity.Reclamo.EstadoReclamo;
import com.consorcioplus.model.entity.UnidadFuncional;
import com.consorcioplus.util.AppColors;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * ReclamosPanel — panel Swing para gestión de reclamos de mantenimiento.
 *
 * Auto-contenido: el usuario elige el consorcio desde el propio panel.
 *
 * Conceptos Java del Módulo 3:
 *  - JTable con modelo personalizado y CellRenderer por estado (colores)
 *  - Cola FIFO: "Atender Siguiente" toma el reclamo más antiguo (PENDIENTE)
 *  - Switch de estados: PENDIENTE → EN_CURSO → RESUELTO | DESCARTADO
 *  - Contador en tiempo real de reclamos pendientes en la cola FIFO
 */
public class ReclamosPanel extends JPanel {

    private final ReclamoController        controller      = new ReclamoController();
    private final ConsorcioController      consorcioCtrl   = new ConsorcioController();
    private final UnidadFuncionalController unidadCtrl     = new UnidadFuncionalController();

    private int idConsorcioActual = -1;

    // --- Selector ---
    private JComboBox<Consorcio> cbConsorcio;

    // --- Componentes UI ---
    private JTable            tblReclamos;
    private DefaultTableModel modelo;
    private JLabel            lblCantidadCola;
    private JButton           btnNuevo;
    private JButton           btnAtenderSiguiente;
    private JButton           btnResolver;
    private JButton           btnDescartar;
    private JButton           btnRefrescar;

    private List<Reclamo> reclamosActuales;

    public ReclamosPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(AppColors.FONDO_PANEL);
        construirUI();
        cargarConsorcios();
    }

    private void construirUI() {

        // ── ENCABEZADO ────────────────────────────────────────────────────
        JPanel pHeader = new JPanel(new BorderLayout(10, 0));
        pHeader.setBackground(AppColors.FONDO_PANEL);

        JLabel titulo = new JLabel("🔧  Gestión de Reclamos");
        titulo.setFont(AppColors.FUENTE_TITULO);
        titulo.setForeground(AppColors.AZUL_OSCURO);
        pHeader.add(titulo, BorderLayout.WEST);

        // Selector de consorcio + indicador de cola FIFO
        JPanel pDerecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pDerecha.setBackground(AppColors.FONDO_PANEL);

        pDerecha.add(new JLabel("Consorcio:"));
        cbConsorcio = new JComboBox<>();
        cbConsorcio.setFont(AppColors.FUENTE_NORMAL);
        cbConsorcio.setPreferredSize(new Dimension(220, 30));
        cbConsorcio.addActionListener(e -> onConsorcioSeleccionado());
        pDerecha.add(cbConsorcio);

        pDerecha.add(Box.createHorizontalStrut(15));
        pDerecha.add(new JLabel("Cola FIFO:"));
        lblCantidadCola = new JLabel("0 pendientes");
        lblCantidadCola.setFont(AppColors.FUENTE_SUBTITULO);
        lblCantidadCola.setForeground(AppColors.COLOR_PENDIENTE);
        pDerecha.add(lblCantidadCola);

        pHeader.add(pDerecha, BorderLayout.EAST);
        add(pHeader, BorderLayout.NORTH);

        // ── TABLA DE RECLAMOS ─────────────────────────────────────────────
        String[] columnas = {"Nro. Reclamo", "UF", "Descripción", "Estado", "Fecha Alta"};
        modelo = new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblReclamos = new JTable(modelo);
        tblReclamos.setFont(AppColors.FUENTE_TABLA);
        tblReclamos.setRowHeight(26);
        tblReclamos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblReclamos.getTableHeader().setFont(AppColors.FUENTE_BOTON);
        tblReclamos.getTableHeader().setBackground(AppColors.AZUL_OSCURO);
        tblReclamos.getTableHeader().setForeground(Color.WHITE);
        tblReclamos.setDefaultRenderer(Object.class, new EstadoColorRenderer());

        tblReclamos.getColumnModel().getColumn(0).setPreferredWidth(140);
        tblReclamos.getColumnModel().getColumn(1).setPreferredWidth(70);
        tblReclamos.getColumnModel().getColumn(2).setPreferredWidth(350);
        tblReclamos.getColumnModel().getColumn(3).setPreferredWidth(110);
        tblReclamos.getColumnModel().getColumn(4).setPreferredWidth(110);

        add(new JScrollPane(tblReclamos), BorderLayout.CENTER);

        // ── BOTONES ───────────────────────────────────────────────────────
        JPanel pBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pBotones.setBackground(AppColors.FONDO_PANEL);
        pBotones.setBorder(new EmptyBorder(10, 0, 0, 0));

        btnNuevo = crearBoton("➕ Nuevo Reclamo", AppColors.AZUL_MEDIO);
        btnNuevo.addActionListener(e -> dialogNuevoReclamo());

        btnAtenderSiguiente = crearBoton("▶ Atender Siguiente (FIFO)", AppColors.COLOR_EN_CURSO);
        btnAtenderSiguiente.setToolTipText("Toma el reclamo PENDIENTE más antiguo y lo pasa a EN_CURSO");
        btnAtenderSiguiente.addActionListener(e -> atenderSiguiente());

        btnResolver = crearBoton("✅ Resolver", AppColors.COLOR_RESUELTO);
        btnResolver.setToolTipText("El reclamo debe estar EN_CURSO para resolverse");
        btnResolver.addActionListener(e -> cambiarEstadoSeleccionado(EstadoReclamo.RESUELTO));

        btnDescartar = crearBoton("❌ Descartar", AppColors.COLOR_DESCARTADO);
        btnDescartar.addActionListener(e -> cambiarEstadoSeleccionado(EstadoReclamo.DESCARTADO));

        btnRefrescar = crearBoton("🔄 Refrescar", AppColors.AZUL_OSCURO);
        btnRefrescar.addActionListener(e -> cargarReclamos());

        pBotones.add(btnNuevo);
        pBotones.add(btnAtenderSiguiente);
        pBotones.add(btnResolver);
        pBotones.add(btnDescartar);
        pBotones.add(btnRefrescar);
        add(pBotones, BorderLayout.SOUTH);
    }

    // =====================================================================
    // Carga de datos
    // =====================================================================

    private void cargarConsorcios() {
        cbConsorcio.removeAllItems();
        try {
            List<Consorcio> lista = consorcioCtrl.listarActivos();
            for (Consorcio c : lista) cbConsorcio.addItem(c);
        } catch (SQLException ex) {
            mostrarError("Error al cargar consorcios: " + ex.getMessage());
        }
    }

    private void onConsorcioSeleccionado() {
        Consorcio c = (Consorcio) cbConsorcio.getSelectedItem();
        if (c == null) return;
        idConsorcioActual = c.getId();
        cargarReclamos();
    }

    public void cargarReclamos() {
        modelo.setRowCount(0);
        reclamosActuales = null;
        if (idConsorcioActual < 0) return;
        try {
            reclamosActuales = controller.obtenerTodos(idConsorcioActual);
            for (Reclamo r : reclamosActuales) {
                modelo.addRow(new Object[]{
                        r.getNroReclamo(),
                        "UF#" + r.getIdUnidad(),
                        r.getDescripcion(),
                        r.getEstado().name(),
                        r.getFechaAlta().toLocalDate()
                });
            }
            lblCantidadCola.setText(controller.cantidadEnCola() + " pendientes");
        } catch (SQLException ex) {
            mostrarError("Error al cargar reclamos: " + ex.getMessage());
        }
    }

    // =====================================================================
    // Eventos de botones
    // =====================================================================

    private void dialogNuevoReclamo() {
        if (idConsorcioActual < 0) { mostrarError("Seleccione un consorcio primero."); return; }

        // Cargar UFs del consorcio para el combo
        List<UnidadFuncional> ufs;
        try {
            ufs = unidadCtrl.listarPorConsorcio(idConsorcioActual);
        } catch (SQLException ex) {
            mostrarError("Error al cargar unidades: " + ex.getMessage());
            return;
        }
        if (ufs.isEmpty()) {
            mostrarError("El consorcio no tiene Unidades Funcionales activas.");
            return;
        }

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        JComboBox<UnidadFuncional> cbUF = new JComboBox<>();
        for (UnidadFuncional uf : ufs) cbUF.addItem(uf);

        JTextArea txtDesc = new JTextArea(3, 20);
        txtDesc.setLineWrap(true);
        txtDesc.setWrapStyleWord(true);

        form.add(new JLabel("Unidad Funcional:"));
        form.add(cbUF);
        form.add(new JLabel("Descripción:"));
        form.add(new JScrollPane(txtDesc));

        int res = JOptionPane.showConfirmDialog(this, form, "Nuevo Reclamo",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        UnidadFuncional ufSeleccionada = (UnidadFuncional) cbUF.getSelectedItem();
        String desc = txtDesc.getText().trim();
        if (ufSeleccionada == null) return;
        if (desc.isEmpty()) { mostrarError("La descripción no puede estar vacía."); return; }

        try {
            Reclamo nuevo = controller.altaReclamo(ufSeleccionada.getId(), desc);
            JOptionPane.showMessageDialog(this,
                    "✅ Reclamo registrado: " + nuevo.getNroReclamo()
                            + "\nEncolado para atención FIFO.",
                    "Reclamo Creado", JOptionPane.INFORMATION_MESSAGE);
            cargarReclamos();
        } catch (SQLException ex) {
            mostrarError("Error al guardar el reclamo: " + ex.getMessage());
        }
    }

    private void atenderSiguiente() {
        if (idConsorcioActual < 0) { mostrarError("Seleccione un consorcio primero."); return; }
        try {
            Reclamo r = controller.atenderSiguiente();
            if (r == null) {
                JOptionPane.showMessageDialog(this,
                        "No hay reclamos PENDIENTES en la cola.",
                        "Cola vacía", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "▶ Atendiendo: " + r.getNroReclamo()
                                + "\n" + r.getDescripcion()
                                + "\n\nEstado cambiado a: EN_CURSO",
                        "Reclamo en Curso", JOptionPane.INFORMATION_MESSAGE);
            }
            cargarReclamos();
        } catch (SQLException ex) {
            mostrarError("Error: " + ex.getMessage());
        }
    }

    private void cambiarEstadoSeleccionado(EstadoReclamo nuevoEstado) {
        int fila = tblReclamos.getSelectedRow();
        if (fila < 0) { mostrarError("Seleccione un reclamo de la tabla."); return; }
        if (reclamosActuales == null || fila >= reclamosActuales.size()) return;
        Reclamo seleccionado = reclamosActuales.get(fila);
        try {
            controller.cambiarEstado(seleccionado, nuevoEstado);
            cargarReclamos();
        } catch (IllegalStateException ex) {
            mostrarError("Transición no válida:\n" + ex.getMessage());
        } catch (SQLException ex) {
            mostrarError("Error al actualizar: " + ex.getMessage());
        }
    }

    // =====================================================================
    // Renderer de colores por estado
    // =====================================================================
    private class EstadoColorRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            if (!isSelected && reclamosActuales != null && row < reclamosActuales.size()) {
                switch (reclamosActuales.get(row).getEstado()) {
                    case PENDIENTE:  c.setBackground(new Color(0xFEF9EC)); break;
                    case EN_CURSO:   c.setBackground(new Color(0xEBF4FF)); break;
                    case RESUELTO:   c.setBackground(new Color(0xECFDF5)); break;
                    case DESCARTADO: c.setBackground(new Color(0xF3F4F6)); break;
                    default:         c.setBackground(Color.WHITE);
                }
            }
            return c;
        }
    }

    // =====================================================================
    // Helpers
    // =====================================================================
    private JButton crearBoton(String texto, Color fondo) {
        JButton btn = new JButton(texto);
        btn.setFont(AppColors.FUENTE_BOTON);
        btn.setBackground(fondo);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void mostrarError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}

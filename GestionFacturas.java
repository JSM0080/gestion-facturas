package t3;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.*;
import java.text.*;
import com.toedter.calendar.JDateChooser;//Antonio soy buena persona y te dejo el enlace https://toedter.com/jcalendar/  +3 puntos eh!!
										 //E incluso te digo como instalarlo si lo necesitas como gesto de buena voluntad (espero recíproca)

abstract class EntidadFactura {
    public abstract double calcularTotal();
}

class DetalleFactura extends EntidadFactura {
    private String descripcion;
    private int cantidad;
    private double precioUnitario;

    public DetalleFactura(String descripcion, int cantidad, double precioUnitario) {
        this.descripcion = descripcion;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    public String getDescripcion() { return descripcion; }
    public int getCantidad() { return cantidad; }
    public double getPrecioUnitario() { return precioUnitario; }

    @Override
    public double calcularTotal() {
        return cantidad * precioUnitario;
    }

    public double getSubtotal() { return calcularTotal(); }

    @Override
    public String toString() {
        return descripcion + " x " + cantidad + " = " + getSubtotal();
    }
}

class Factura extends EntidadFactura {
    private int id;
    private String cliente;
    private String fecha;
    private ArrayList<DetalleFactura> detalles;

    public Factura(int id, String cliente, String fecha) {
        this.id = id;
        this.cliente = cliente;
        this.fecha = fecha;
        this.detalles = new ArrayList<>();
    }

    public void agregarDetalle(DetalleFactura d) { detalles.add(d); }
    public void eliminarDetalle(int index) { if (index >= 0 && index < detalles.size()) detalles.remove(index); }

    @Override
    public double calcularTotal() {
        return detalles.stream().mapToDouble(DetalleFactura::calcularTotal).sum();
    }

    public double getTotal() { return calcularTotal(); }
    public int getId() { return id; }
    public String getCliente() { return cliente; }
    public String getFecha() { return fecha; }
    public ArrayList<DetalleFactura> getDetalles() { return detalles; }
}

public class GestionFacturas extends JFrame {
    private JTable tablaFacturas, tablaDetalles;
    private DefaultTableModel modeloFacturas, modeloDetalles;
    private JComboBox<String> comboClientes;
    private JDateChooser dateChooser;
    private JTextField txtDescripcion, txtCantidad, txtPrecio;
    private JButton btnAddFactura, btnDelFactura, btnAddDetalle, btnDelDetalle;
    private JLabel lblTotal;
    private ArrayList<Factura> listaFacturas;
    private int facturaId = 1;

    public GestionFacturas() {
        setTitle("Gestión de Facturación");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setLayout(null);
        initComponents();
        initListeners();
    }

    private void initComponents() {
        listaFacturas = new ArrayList<>();

        JLabel lblCliente = new JLabel("Cliente:");
        lblCliente.setBounds(20, 20, 60, 25);
        add(lblCliente);

        comboClientes = new JComboBox<>(new String[]{"Jorge Nitales", "María Umpa Jote", "Manuela", "Mark y Khom's Company"});
        comboClientes.setBounds(80, 20, 200, 25);
        add(comboClientes);

        JLabel lblFecha = new JLabel("Fecha:");
        lblFecha.setBounds(300, 20, 50, 25);
        add(lblFecha);

        dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("dd/MM/yyyy");
        dateChooser.setBounds(360, 20, 120, 25);
        add(dateChooser);

        btnAddFactura = new JButton("Añadir Factura");
        btnAddFactura.setBounds(500, 20, 140, 25);
        add(btnAddFactura);

        btnDelFactura = new JButton("Eliminar Factura");
        btnDelFactura.setBounds(660, 20, 140, 25);
        add(btnDelFactura);

        modeloFacturas = new DefaultTableModel(new String[]{"ID", "Cliente", "Fecha", "Total"}, 0);
        tablaFacturas = new JTable(modeloFacturas);
        JScrollPane spFacturas = new JScrollPane(tablaFacturas);
        spFacturas.setBounds(20, 60, 940, 130);
        add(spFacturas);

        JLabel lblDesc = new JLabel("Descripción:");
        lblDesc.setBounds(20, 210, 100, 25);
        add(lblDesc);

        txtDescripcion = new JTextField();
        txtDescripcion.setBounds(120, 210, 150, 25);
        add(txtDescripcion);

        JLabel lblCant = new JLabel("Cantidad:");
        lblCant.setBounds(290, 210, 80, 25);
        add(lblCant);

        txtCantidad = new JTextField();
        txtCantidad.setBounds(360, 210, 60, 25);
        add(txtCantidad);

        JLabel lblPrecio = new JLabel("Precio:");
        lblPrecio.setBounds(440, 210, 80, 25);
        add(lblPrecio);

        txtPrecio = new JTextField();
        txtPrecio.setBounds(500, 210, 60, 25);
        add(txtPrecio);

        btnAddDetalle = new JButton("Añadir Detalle");
        btnAddDetalle.setBounds(580, 210, 140, 25);
        add(btnAddDetalle);

        btnDelDetalle = new JButton("Eliminar Detalle");
        btnDelDetalle.setBounds(740, 210, 140, 25);
        add(btnDelDetalle);

        modeloDetalles = new DefaultTableModel(new String[]{"Descripción", "Cantidad", "Precio Unitario", "Subtotal"}, 0);
        tablaDetalles = new JTable(modeloDetalles);
        JScrollPane spDetalles = new JScrollPane(tablaDetalles);
        spDetalles.setBounds(20, 250, 940, 280);
        add(spDetalles);

        lblTotal = new JLabel("Total Factura: 0.0");
        lblTotal.setHorizontalAlignment(SwingConstants.RIGHT);
        lblTotal.setFont(new Font("Arial", Font.BOLD, 16));
        lblTotal.setBounds(680, 540, 280, 30);
        add(lblTotal);
    }

    private void initListeners() {
        btnAddFactura.addActionListener(e -> agregarFactura());
        btnDelFactura.addActionListener(e -> eliminarFactura());
        btnAddDetalle.addActionListener(e -> agregarDetalle());
        btnDelDetalle.addActionListener(e -> eliminarDetalle());
        tablaFacturas.getSelectionModel().addListSelectionListener(e -> mostrarDetallesFactura());
    }

    private void agregarFactura() {
        if (dateChooser.getDate() == null) {
            JOptionPane.showMessageDialog(this, "Seleccione una fecha", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String fecha = new SimpleDateFormat("dd/MM/yyyy").format(dateChooser.getDate());
        String cliente = comboClientes.getSelectedItem().toString();

        Factura factura = new Factura(facturaId++, cliente, fecha);
        listaFacturas.add(factura);
        modeloFacturas.addRow(new Object[]{factura.getId(), cliente, fecha, 0.0});
        dateChooser.setDate(null);
    }

    private void eliminarFactura() {
        int row = tablaFacturas.getSelectedRow();
        if (row >= 0) {
            listaFacturas.remove(row);
            modeloFacturas.removeRow(row);
            modeloDetalles.setRowCount(0);
            lblTotal.setText("Total Factura: 0.0");
        }
    }

    private void mostrarDetallesFactura() {
        int index = tablaFacturas.getSelectedRow();
        if (index == -1) return;

        Factura f = listaFacturas.get(index);
        modeloDetalles.setRowCount(0);
        for (DetalleFactura d : f.getDetalles()) {
            modeloDetalles.addRow(new Object[]{d.getDescripcion(), d.getCantidad(), d.getPrecioUnitario(), d.getSubtotal()});
        }
        modeloFacturas.setValueAt(f.getTotal(), index, 3);
        lblTotal.setText("Total Factura: " + f.getTotal());
    }

    private void agregarDetalle() {
        int index = tablaFacturas.getSelectedRow();
        if (index == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una factura.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            String desc = txtDescripcion.getText();
            int cant = Integer.parseInt(txtCantidad.getText());
            double precio = Double.parseDouble(txtPrecio.getText());

            DetalleFactura detalle = new DetalleFactura(desc, cant, precio);
            Factura f = listaFacturas.get(index);
            f.agregarDetalle(detalle);
            mostrarDetallesFactura();

            txtDescripcion.setText("");
            txtCantidad.setText("");
            txtPrecio.setText("");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Cantidad y precio deben ser numéricos.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarDetalle() {
        int indexFactura = tablaFacturas.getSelectedRow();
        int indexDetalle = tablaDetalles.getSelectedRow();
        if (indexFactura != -1 && indexDetalle != -1) {
            Factura f = listaFacturas.get(indexFactura);
            f.eliminarDetalle(indexDetalle);
            mostrarDetallesFactura();
        }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new GestionFacturas().setVisible(true));
    }
}

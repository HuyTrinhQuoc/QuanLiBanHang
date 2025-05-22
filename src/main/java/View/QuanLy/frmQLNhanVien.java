package View.QuanLy;

import Custom.MyComboBoxRenderer;
import Custom.MyFileChooser;
import Custom.XuLyFileExcel;
import DAO.NhanVienDAO;
import POJO.NhanVien;
import UIS.MsgBox;
import static java.awt.Desktop.getDesktop;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nguyen Hoang Phuc
 */
public class frmQLNhanVien extends javax.swing.JInternalFrame {
    NhanVienDAO dao;
    File fileAnhSP; // Lưu file ảnh của nhân viên
    
    // Constructor của form quản lý nhân viên
    public frmQLNhanVien() {
        initComponents(); // Khởi tạo giao diện
        // Ẩn cột thứ 7 (index 6) trong bảng
        tb.getColumnModel().getColumn(6).setWidth(0);
        tb.getColumnModel().getColumn(6).setMinWidth(0);
        tb.getColumnModel().getColumn(6).setMaxWidth(0);
        
        // Đặt renderer tùy chỉnh cho combobox chức vụ
        cboChucVu.setRenderer(new MyComboBoxRenderer("Vui lòng chọn chức vụ"));
        cboChucVu.setSelectedIndex(-1); // Chưa chọn mặc định
        enableBtn(true); // Kích hoạt các nút thao tác
        enableText(false); // Vô hiệu hóa các ô nhập liệu
        dao = new NhanVienDAO(); // Khởi tạo DAO
        fillToTable(dao.selectAll()); // Hiển thị dữ liệu lên bảng
        selectTable(); // Chọn dòng đầu tiên (nếu có)
    }

    // Phương thức clearForm dùng để xóa trắng toàn bộ thông tin trong form nhập liệu
    private void clearForm() {
        // Xóa nội dung ô nhập mã nhân viên
        txtMaNV.setText("");
        // Xóa nội dung ô nhập họ tên nhân viên
        txtTenNV.setText("");
        // Bỏ chọn giới tính (RadioButton)
        grbSex.clearSelection();
        // Đặt combobox chức vụ về trạng thái chưa chọn (nếu hỗ trợ chọn trống)
        cboChucVu.setSelectedIndex(-1); // -1 = không chọn gì cả
        // Xóa nội dung ô nhập số điện thoại
        txtSDT.setText("");
        // Bỏ chọn checkbox trạng thái hoạt động
        cbActive.setSelected(false);
        // Xóa ảnh đại diện nếu đang hiển thị
        txtImg.setIcon(null);
    }
    
    // Lấy thông tin nhân viên từ form và trả về đối tượng NhanVien
    private NhanVien getForm() {
        NhanVien nv = new NhanVien(); // tạo đối tượng nhân viên
        nv.setMaNV(txtMaNV.getText()); // thiết lập mã nhân viên cho đối tượng nhân viên
        nv.setHoTen(txtTenNV.getText()); // thiết lập tên nhân viên cho đôi tượng nhân viên
        nv.setSoDT(txtSDT.getText());   // thiết lập số điện thoại cho đối tượng nhân viên
        nv.setGioiTinh(rbNam.isSelected()); // thiết lập giới tính cho đối tượng nhân viên (true nếu chọn Nam)
        nv.setActive(cbActive.isSelected()); // thiết lập trạng thái tài khoản (true nếu checkbox được chọn)
        nv.setAvatar(String.valueOf(fileAnhSP.getName())); // Tên file ảnh
        nv.setChucVu(cboChucVu.getSelectedItem().toString()); // Chức vụ được chọn
        return nv; // trả về đối tượng NhanVien
    }

    // Thêm hoặc cập nhật nhân viên
    void InsertOrUpdate() {
        NhanVien nv = getForm(); // Lấy thông tin từ form
        dao.insertOrUpdate(nv); // Thêm hoặc cập nhật vào CSDL
        fillToTable(dao.selectAll()); // Cập nhật bảng hiển thị
        this.clearForm(); // Xóa trắng form sau khi thêm
        luuFileAnh(); // Lưu file ảnh vào thư mục
        MsgBox.alert(this, "Đã thêm vào cơ sở dữ liệu!");
    }
    
    private void insertNV() {
        NhanVien nv = getForm();
        try{
        int result = dao.insert(nv);
        if(result == 1){
            MsgBox.alert(this, "Thêm nhân viên thành công");
            fillToTable(dao.selectAll());
        }
        } catch(SQLException ex){
            if (ex.getErrorCode() == 1062) {
                MsgBox.alert(this, "Lỗi: Mã nhân viên đã tồn tại!");
            }
            else {
                Logger.getLogger(frmQLNhanVien.class.getName()).log(Level.SEVERE, "Lỗi khi thêm nhân viên", ex);
                MsgBox.alert(this, "Lỗi: " + ex.getMessage());
            }
        } catch (Exception ex) {
            Logger.getLogger(frmQLNhanVien.class.getName()).log(Level.SEVERE, "Lỗi khi thêm nhân viên", ex);
            MsgBox.alert(this, "Lỗi không xác định: " + ex.getMessage());
        }
    }
    
     // Xử lý và trả về icon ảnh từ đường dẫn
    private ImageIcon getAnhSP(String src) {
        src = src.trim().equals("") ? "default.png" : src; // Nếu rỗng thì dùng ảnh mặc định
        BufferedImage img = null;
        File fileImg = new File(src);

        // Nếu file không tồn tại, dùng ảnh mặc định
        if (!fileImg.exists()) {
            src = "default.png";
            fileImg = new File("Assets/Img/" + src);
        }

        try {
            img = ImageIO.read(fileImg); // Đọc ảnh
            fileAnhSP = new File(src); // Gán file hiện tại
        } catch (IOException e) {
            fileAnhSP = new File("Assets/Img/default.png"); // Nếu lỗi, dùng ảnh dự phòng
        }

        if (img != null) {
            // Thay đổi kích thước ảnh để hiển thị
            Image dimg = img.getScaledInstance(130, 117, Image.SCALE_SMOOTH);
            return new ImageIcon(dimg);
        }

        return null;
    }

    private void luuFileAnh() {
    BufferedImage bImage;
    try {
        File initialImage = new File(fileAnhSP.getPath());
        bImage = ImageIO.read(initialImage); // Đọc ảnh

        // Tạo thư mục nếu chưa tồn tại
        File folder = new File("Assets/Img");
        if (!folder.exists()) {
            folder.mkdirs(); // tạo thư mục Assets/Img nếu chưa có
        }

        // Lưu ảnh
        File outputFile = new File(folder, fileAnhSP.getName());
        ImageIO.write(bImage, "png", outputFile);
    } catch (IOException ex) {
        MsgBox.alert(this, "Lỗi khi lưu ảnh: " + ex.getMessage());
    }
}


    // Mở file bằng phần mềm mặc định của hệ điều hành
    private void openFile(String file) {
        try {
            File path = new File(file);
            getDesktop().open(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Xử lý khi người dùng chọn ảnh từ máy
    private void xuLyChonAnh() {
        JFileChooser fileChooser = new MyFileChooser("Assets/Img/");
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Tệp hình ảnh", "jpg", "png", "jpeg");
        fileChooser.setFileFilter(filter); // Lọc định dạng ảnh
        int returnVal = fileChooser.showOpenDialog(null);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            fileAnhSP = fileChooser.getSelectedFile(); // Lưu file ảnh đã chọn
            txtImg.setIcon(getAnhSP(fileAnhSP.getPath())); // Hiển thị ảnh lên label
        }
    }

    // Hiển thị danh sách nhân viên lên bảng
    private void fillToTable(List<NhanVien> a) {
        DefaultTableModel model = (DefaultTableModel) tb.getModel();
        model.setRowCount(0); // Xóa hết dữ liệu cũ
        for (NhanVien p : a) {
            model.addRow(new Object[]{
                p.getMaNV(), p.getHoTen(), p.getSoDT(), p.getChucVu(),
                p.isGioiTinh() ? "Nam" : "Nữ", p.isActive(), p.getAvatar()
            });
        }
    }


    private ImageIcon loadImage(String tenHinh) {
        ImageIcon ii = new ImageIcon("Assets/Img/" + tenHinh);
        return ii;
    }

    // kiểm tra thông tin người dùng nhập vào form
    // nếu thông tin đúng thì trả về true sai trả về false
    boolean checkVal() {
        // Mã Nhân viên trống hoặc chỉ chứa khoảng trắng  
        if (txtMaNV.getText().trim().isEmpty()) {
            MsgBox.alert(this, "Bạn chưa nhập mã nhân viên!");
            return false;
        } 
        // Mã nhân viên dài hơn 6 ký tự
        else if (txtMaNV.getText().length() > 6) {
            MsgBox.alert(this, "Mã nhân viên tối đa chỉ 6 ký tự!");
            return false;
        } 
        // Tên nhân viên trống hoặc chỉ chứa khoảng trắng
        if (txtTenNV.getText().trim().isEmpty()) {
            MsgBox.alert(this, "Bạn chưa nhập tên nhân viên!");
            return false;
        }
        
        // Chưa chọn giới tính cho nhân viên 
        if (!rbNam.isSelected() && !rbNu.isSelected()) {
            MsgBox.alert(this, "Bạn chưa chọn giới tính cho nhân viên!");
            return false;
        }
       
        // Chưa chọn chức vụ cho nhân viên
        if (cboChucVu.getSelectedIndex() == -1) {
            MsgBox.alert(this, "Bạn chưa chọn chức vụ!");
            return false;
        }
        
        // Số điện thoại trống hoặc chỉ chứa khoảng trắng
        if (txtSDT.getText().trim().isEmpty()) {
            MsgBox.alert(this, "Bạn chưa nhập sdt cho nhân viên!");
            return false;
        }
        // Số điện thoại chỉ được chứa 10 ký tự số
        else if (!txtSDT.getText().trim().matches("\\d{10}")) {
            MsgBox.alert(this, "Số điện thoại phải gồm đúng 10 chữ số và không chứa ký tự khác!");
            return false;
        }
        if (fileAnhSP == null) {
            MsgBox.alert(this, "Vui lòng chọn hình ảnh cho nhân viên!");
            return false;
        }
        return true;
    }

    // Phương thức thiết lập sự kiện khi chọn dòng trong bảng
    private void selectTable() {
        // Cho phép chọn ô trong bảng (thay vì cả dòng)
        tb.setCellSelectionEnabled(true);
        
        // lấy mô hình lựa chọn của bảng
        ListSelectionModel select = tb.getSelectionModel();
        // thêm bộ lắng nghe sự kiện thay đổi lựa chọn
        select.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                // lấy chỉ số hàng được chọn
                int row = tb.getSelectedRow();
                
                // nếu có hàng được chọn
                if (row >= 0) {
                    // gán giá trị từ bảng vào các thành phàn trên form
                    txtMaNV.setText((String) tb.getValueAt(row, 0));
                    txtTenNV.setText((String) tb.getValueAt(row, 1));
                    txtSDT.setText(String.valueOf(tb.getValueAt(row, 2)));
                    cboChucVu.setSelectedItem(tb.getValueAt(row, 3));
                    
                    //lấy giới tình và thiết lập radio tương ứng
                    Boolean sex = (String.valueOf(tb.getValueAt(row, 4)) == "Nam") ? true : false;
                    rbNam.setSelected(sex);
                    rbNu.setSelected(!sex);
                    
                    // trạng thái hoạt động của (checkbox)
                    cbActive.setSelected(((Boolean) tb.getValueAt(row, 5)).booleanValue());
                    
                    // Thiết lập hình ảnh từ đường dẫn ảnh trong cột thứ 6
                    int width = 130; // chiều rộng mong muốn của JLabel
                    int height = 117; // chiều cao mong muốn của JLabel
                    Image img = loadImage(String.valueOf(tb.getValueAt(row, 6))).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                    
                    // tạo đối tượng file cho hình ảnh hiện tại
                    fileAnhSP = new File("Assets/Img/" + String.valueOf(tb.getValueAt(row, 6)));
                    
                    // gán ảnh vào label hiển thị ảnh
                    txtImg.setIcon(new ImageIcon(img));

                }
            }
        });
    }

    /// Kích hoạt hoặc vô hiệu hóa các nút thao tác chính
    // enable == true: các nút được bật
    // enbale == false: các nút bị tăt
    private void enableBtn(Boolean enable) {
        btnInsert.setEnabled(enable); // kích hoạt hoặc vô hiệu nút "Thêm"
        btnUpdate.setEnabled(enable); // kích hoạt hoặc vô hiệu nút "Cập nhật"
        btnRemove.setEnabled(enable); // kích hoạt hoặc vô hiệu nút "Xoá"
        btnSave.setEnabled(!enable); // Nút lưu chỉ bật khi đang trong chế độ chỉnh sửa
        btnExPortEX.setEnabled(enable); // kích hoạt hoặc vô hiệu nút "Xuất"
        btnInportEX.setEnabled(enable); // kích hoạt hoặc vô hiệu nút "Nhập"
        btnResertPass.setEnabled(enable); // kích hoạt hoặc vô hiệu "Reset Password"
        btnCancel.setEnabled(!enable); // kích hoạt hoặc vô hiệu "Quyền"
    }
    
    // Phương thức enableText dùng để bật hoặc tắt khả năng nhập liệu của các thành phần giao diện
    // Nếu enable == true: cho phép người dùng nhập/chỉnh sửa dữ liệu
    // Nếu enable == false: khóa không cho người dùng nhập/chỉnh sửa dữ liệu
    private void enableText(Boolean enable) {
        // Bật/tắt khả năng nhập liệu của ô mã nhân viên (editable)
        txtMaNV.setEditable(enable);
        // Bật/tắt hai nút chọn giới tính (radio button)
        rbNam.setEnabled(enable);
        rbNu.setEnabled(enable);
        // Bật/tắt combobox chọn chức vụ
        cboChucVu.setEnabled(enable);
        // Bật/tắt khả năng nhập liệu cho ô họ tên nhân viên
        txtTenNV.setEditable(enable);
        // Bật/tắt ô nhập số điện thoại
        txtSDT.setEnabled(enable);
        // Bật/tắt checkbox trạng thái hoạt động
        cbActive.setEnabled(enable);
        // Bật/tắt nút chọn ảnh đại diện
        btnChooseImage.setEnabled(enable);
    }
    
    private void xuLyXuatFileExcel() {
        XuLyFileExcel xuatFile = new XuLyFileExcel();
        xuatFile.xuatExcel(tb);
    }

    private void xuLyNhapFileExcel() {
        XuLyFileExcel nhapFile = new XuLyFileExcel();
        nhapFile.nhapExcel(tb);
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        grbSex = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        txtMaNV = new javax.swing.JTextField();
        txtTenNV = new javax.swing.JTextField();
        cboChucVu = new javax.swing.JComboBox<>();
        pnImage = new javax.swing.JPanel();
        txtImg = new javax.swing.JLabel();
        btnChooseImage = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        txtSearch = new javax.swing.JTextField();
        btnSearch = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tb = new javax.swing.JTable();
        btnInsert = new javax.swing.JButton();
        btnRemove = new javax.swing.JButton();
        btnUpdate = new javax.swing.JButton();
        btnInportEX = new javax.swing.JButton();
        btnExPortEX = new javax.swing.JButton();
        btnSave = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        rbNam = new javax.swing.JRadioButton();
        rbNu = new javax.swing.JRadioButton();
        cbActive = new javax.swing.JCheckBox();
        btnResertPass = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        txtSDT = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();

        setClosable(true);
        setTitle("Quản lý nhân viên");

        jLabel1.setFont(new java.awt.Font("Times New Roman", 1, 26)); // NOI18N
        jLabel1.setText("QUẢN LÝ NHÂN VIÊN");

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel2.setText("Mã NV:");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel4.setText("Tên NV:");

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel5.setText("Giới tính:");

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel6.setText("Chức vụ:");

        cboChucVu.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Quản lý", "Lễ Tân", "Nhân Viên Phục Vụ" }));
        cboChucVu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboChucVuActionPerformed(evt);
            }
        });

        pnImage.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        javax.swing.GroupLayout pnImageLayout = new javax.swing.GroupLayout(pnImage);
        pnImage.setLayout(pnImageLayout);
        pnImageLayout.setHorizontalGroup(
            pnImageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnImageLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(txtImg, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        pnImageLayout.setVerticalGroup(
            pnImageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(txtImg, javax.swing.GroupLayout.DEFAULT_SIZE, 117, Short.MAX_VALUE)
        );

        btnChooseImage.setText("Chọn ảnh");
        btnChooseImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnChooseImageActionPerformed(evt);
            }
        });

        jLabel3.setText("Tìm kiếm");

        txtSearch.setPreferredSize(new java.awt.Dimension(104, 27));

        btnSearch.setText("Tìm Kiếm");
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
            }
        });

        jScrollPane1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        jScrollPane1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jScrollPane1.setRowHeaderView(null);

        tb.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "Mã NV", "Họ Tên NV", "SDT", "Chức vụ", "Giới tính", "Active", "Avata"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.String.class, java.lang.Boolean.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        tb.setRowSelectionAllowed(false);
        tb.setShowGrid(true);
        jScrollPane1.setViewportView(tb);

        btnInsert.setText("Thêm");
        btnInsert.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnInsertActionPerformed(evt);
            }
        });

        btnRemove.setText("Xóa");
        btnRemove.setPreferredSize(new java.awt.Dimension(84, 27));
        btnRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveActionPerformed(evt);
            }
        });

        btnUpdate.setText("Sửa");
        btnUpdate.setPreferredSize(new java.awt.Dimension(84, 27));
        btnUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateActionPerformed(evt);
            }
        });

        btnInportEX.setText("Nhập");
        btnInportEX.setPreferredSize(new java.awt.Dimension(84, 27));
        btnInportEX.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnInportEXActionPerformed(evt);
            }
        });

        btnExPortEX.setText("Xuất");
        btnExPortEX.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExPortEXActionPerformed(evt);
            }
        });

        btnSave.setText("Lưu");
        btnSave.setPreferredSize(new java.awt.Dimension(84, 27));
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel7.setText("Hiệu lực");

        grbSex.add(rbNam);
        rbNam.setText("Nam");

        grbSex.add(rbNu);
        rbNu.setText("Nữ");

        cbActive.setText("Hiệu lực");

        btnResertPass.setText("Resert password");
        btnResertPass.setPreferredSize(new java.awt.Dimension(84, 27));
        btnResertPass.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnResertPassActionPerformed(evt);
            }
        });

        btnCancel.setText("Huỷ");
        btnCancel.setPreferredSize(new java.awt.Dimension(84, 27));
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel8.setText("SDT:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(135, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 888, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(btnInsert, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(15, 15, 15)
                        .addComponent(btnRemove, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(17, 17, 17)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnResertPass, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(19, 19, 19)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(22, 22, 22)
                                .addComponent(btnInportEX, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(19, 19, 19)
                                .addComponent(btnExPortEX, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(120, 120, 120))
            .addGroup(layout.createSequentialGroup()
                .addGap(251, 251, 251)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(95, 95, 95)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(jLabel1)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(38, 38, 38)
                                .addComponent(btnSearch))))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel4)
                            .addComponent(jLabel5)
                            .addComponent(jLabel6)
                            .addComponent(jLabel8)
                            .addComponent(jLabel7))
                        .addGap(25, 25, 25)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cbActive)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(txtMaNV, javax.swing.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE)
                                    .addComponent(txtTenNV, javax.swing.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE)
                                    .addComponent(cboChucVu, 0, 220, Short.MAX_VALUE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(rbNam)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(rbNu))
                                    .addComponent(txtSDT))
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 183, Short.MAX_VALUE)
                                        .addComponent(pnImage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(210, 210, 210)
                                        .addComponent(btnChooseImage)
                                        .addGap(0, 0, Short.MAX_VALUE)))))))
                .addContainerGap(270, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtSearch, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 32, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(btnSearch, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(pnImage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnChooseImage)
                        .addGap(53, 53, 53))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(txtMaNV, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(txtTenNV, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(rbNam)
                            .addComponent(rbNu))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cboChucVu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtSDT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel8))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7)
                            .addComponent(cbActive))
                        .addGap(17, 17, 17)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnInsert)
                    .addComponent(btnRemove, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnInportEX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnExPortEX)
                    .addComponent(btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnResertPass, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(24, 24, 24))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
 
    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        if (dao.FIND_NhanVien(String.valueOf("%" + txtSearch.getText() + "%")) == null)
            MsgBox.alert(this, "Không tìm thấy nhân viên");
        else
            fillToTable(dao.FIND_NhanVien(String.valueOf("%" + txtSearch.getText() + "%")));
    }//GEN-LAST:event_btnSearchActionPerformed

    private void btnInsertActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInsertActionPerformed
        // xóa trắng toàn bộ thông tin trong form nhập liệu
        clearForm();
        // Kích hoạt các nút thao tác chính
        enableBtn(false);
        // bật khả năng nhập liệu của các thành phần giao diện
        enableText(true);
    }//GEN-LAST:event_btnInsertActionPerformed

    private void btnChooseImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnChooseImageActionPerformed
        // TODO add your handling code here:
        xuLyChonAnh();
    }//GEN-LAST:event_btnChooseImageActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        if (!checkVal()) {
            return; // Nếu dữ liệu không hợp lệ thì thoát
        } else {
            // Nếu dữ liệu hợp lệ thì thực hiện thêm mới hoặc cập nhật
            insertNV();
            luuFileAnh();
        }
        // Sau khi lưu xong thì bật lại các nút chức năng và khóa các ô nhập
    }//GEN-LAST:event_btnSaveActionPerformed
    
    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
        if (txtMaNV.getText().isEmpty()) {
            MsgBox.alert(this, "Vui lòng chọn hoặc tìm kiếm thông tin nhân viên bạn muốn sửa?");
        } else {
            enableBtn(false);
            enableText(true);
            txtMaNV.setEnabled(false);
        }

    }//GEN-LAST:event_btnUpdateActionPerformed

    private void btnRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveActionPerformed
         
//        if (!txtMaNV.getText().isEmpty()) {
//            if (MsgBox.confirm(this, "Bạn có chắc chắn muốn xóa nhân viên " + txtTenNV.getText() + " ra khỏi danh sách không?")) {
//                dao.delete(txtMaNV.getText());
//                fillToTable(dao.selectAll());
//                this.clearForm(); // xóa trắng form
//            }
//        } else {
//            MsgBox.alert(this, "Vui lòng chọn hoặc tìm kiếm món bạn muốn sữa?");
//        }
    }//GEN-LAST:event_btnRemoveActionPerformed

    private void btnInportEXActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInportEXActionPerformed
        // TODO add your handling code here:
        xuLyNhapFileExcel();
        enableText(false);
        enableBtn(false);
        btnInportEX.setEnabled(true);
    }//GEN-LAST:event_btnInportEXActionPerformed
    
    private void btnExPortEXActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExPortEXActionPerformed
//        xuLyXuatFileExcel();        // TODO add your handling code here:
//        try {
//            JFileChooser jFileChooser=new JFileChooser();
//            jFileChooser.showSaveDialog(this);
//            File saveFile=jFileChooser.getSelectedFile();
//            if(saveFile!=null)
//            {
//                saveFile=new File(saveFile.toString()+".xlsx");
//                Workbook wb = new XSSFWorkbook();
//                Sheet sheet=wb.createSheet("Thực đơn");
//                Row rowCol=sheet.createRow(0);
//                for (int i = 0; i < tbMenu.getColumnCount(); i++) {
//                    Cell cell=rowCol.createCell(i);
//                    cell.setCellValue(tbMenu.getColumnName(i));
//                }
//                for(int i=0;i<tbMenu.getRowCount();i++)
//                {
//                    Row row=sheet.createRow(i+1);
//                    for (int j = 0; j < tbMenu.getColumnCount(); j++) {
//                        Cell cell=row.createCell(j);
//                        if(tbMenu.getValueAt(i, j)!=null)
//                        {
//                            cell.setCellValue(tbMenu.getValueAt(i, j).toString());
//                        }
//                    }
//                }
//                FileOutputStream out=new FileOutputStream(new File(saveFile.toString()));
//                wb.write(out);
//                wb.close();
//                out.close();
//                OpenFile(saveFile.toString());
//            }
//            else
//            {
//                MsgBox.alert(this, "Error");
//            }
//        } catch (FileNotFoundException e) {
//        }catch(IOException ex){}
        xuLyXuatFileExcel();
    }//GEN-LAST:event_btnExPortEXActionPerformed

    private void btnResertPassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResertPassActionPerformed
   
    }//GEN-LAST:event_btnResertPassActionPerformed

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        enableBtn(true);
        enableText(false);
        clearForm();
    }//GEN-LAST:event_btnCancelActionPerformed

    private void cboChucVuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboChucVuActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cboChucVuActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnChooseImage;
    private javax.swing.JButton btnExPortEX;
    private javax.swing.JButton btnInportEX;
    private javax.swing.JButton btnInsert;
    private javax.swing.JButton btnRemove;
    private javax.swing.JButton btnResertPass;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JCheckBox cbActive;
    private javax.swing.JComboBox<String> cboChucVu;
    private javax.swing.ButtonGroup grbSex;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel pnImage;
    private javax.swing.JRadioButton rbNam;
    private javax.swing.JRadioButton rbNu;
    private javax.swing.JTable tb;
    private javax.swing.JLabel txtImg;
    private javax.swing.JTextField txtMaNV;
    private javax.swing.JTextField txtSDT;
    private javax.swing.JTextField txtSearch;
    private javax.swing.JTextField txtTenNV;
    // End of variables declaration//GEN-END:variables
}

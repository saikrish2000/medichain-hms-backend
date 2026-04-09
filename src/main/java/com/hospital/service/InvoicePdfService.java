package com.hospital.service;

import com.hospital.entity.Invoice;
import com.hospital.entity.InvoiceItem;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.InvoiceRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoicePdfService {

    private final InvoiceRepository invoiceRepo;

    private static final BaseColor BRAND_PURPLE = new BaseColor(108, 99, 255);
    private static final BaseColor LIGHT_GRAY   = new BaseColor(248, 249, 255);
    private static final BaseColor DARK_TEXT    = new BaseColor(26, 26, 46);
    private static final Font TITLE_FONT   = new Font(Font.FontFamily.HELVETICA, 22, Font.BOLD, BaseColor.WHITE);
    private static final Font HEADING_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, DARK_TEXT);
    private static final Font NORMAL_FONT  = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, DARK_TEXT);
    private static final Font SMALL_FONT   = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, BaseColor.GRAY);
    private static final Font TOTAL_FONT   = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BRAND_PURPLE);

    public byte[] generateInvoicePdf(Long invoiceId) {
        Invoice invoice = invoiceRepo.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", invoiceId));

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 40, 40, 40, 40);
            PdfWriter.getInstance(doc, out);
            doc.open();

            // ── Header Banner ──
            PdfPTable header = new PdfPTable(1);
            header.setWidthPercentage(100);
            PdfPCell headerCell = new PdfPCell();
            headerCell.setBackgroundColor(BRAND_PURPLE);
            headerCell.setPadding(20);
            headerCell.setBorder(Rectangle.NO_BORDER);
            Paragraph title = new Paragraph("🏥  MediChain HMS", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            headerCell.addElement(title);
            Paragraph subtitle = new Paragraph("INVOICE", new Font(Font.FontFamily.HELVETICA, 14, Font.NORMAL, BaseColor.WHITE));
            subtitle.setAlignment(Element.ALIGN_CENTER);
            headerCell.addElement(subtitle);
            header.addCell(headerCell);
            doc.add(header);
            doc.add(Chunk.NEWLINE);

            // ── Invoice Meta ──
            PdfPTable meta = new PdfPTable(2);
            meta.setWidthPercentage(100);
            meta.setSpacingBefore(10);

            addMetaRow(meta, "Invoice Number:", invoice.getInvoiceNumber() != null ? invoice.getInvoiceNumber() : "INV-" + invoice.getId());
            addMetaRow(meta, "Date:", invoice.getCreatedAt() != null
                    ? invoice.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")) : "-");
            addMetaRow(meta, "Status:", invoice.getStatus());
            if (invoice.getPaidAt() != null)
                addMetaRow(meta, "Paid On:", invoice.getPaidAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")));
            if (invoice.getPaymentMethod() != null)
                addMetaRow(meta, "Payment Method:", invoice.getPaymentMethod());
            if (invoice.getTransactionId() != null)
                addMetaRow(meta, "Transaction ID:", invoice.getTransactionId());

            doc.add(meta);
            doc.add(Chunk.NEWLINE);

            // ── Patient Info ──
            if (invoice.getPatient() != null && invoice.getPatient().getUser() != null) {
                PdfPTable patientTable = new PdfPTable(1);
                patientTable.setWidthPercentage(100);
                PdfPCell patCell = new PdfPCell();
                patCell.setBackgroundColor(LIGHT_GRAY);
                patCell.setPadding(12);
                patCell.setBorderColor(new BaseColor(200, 210, 255));
                Paragraph patHeading = new Paragraph("Patient Details", HEADING_FONT);
                patCell.addElement(patHeading);
                var user = invoice.getPatient().getUser();
                patCell.addElement(new Paragraph("Name: " + user.getFirstName() + " " + user.getLastName(), NORMAL_FONT));
                patCell.addElement(new Paragraph("Email: " + user.getEmail(), NORMAL_FONT));
                if (user.getPhone() != null)
                    patCell.addElement(new Paragraph("Phone: " + user.getPhone(), NORMAL_FONT));
                patCell.addElement(new Paragraph("Patient ID: " + invoice.getPatient().getPatientIdNumber(), NORMAL_FONT));
                patientTable.addCell(patCell);
                doc.add(patientTable);
                doc.add(Chunk.NEWLINE);
            }

            // ── Items Table ──
            PdfPTable itemsTable = new PdfPTable(new float[]{4, 1, 2, 2});
            itemsTable.setWidthPercentage(100);
            itemsTable.setSpacingBefore(5);

            // Table Header
            String[] headers = {"Description", "Qty", "Unit Price", "Total"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE)));
                cell.setBackgroundColor(BRAND_PURPLE);
                cell.setPadding(8);
                cell.setBorder(Rectangle.NO_BORDER);
                itemsTable.addCell(cell);
            }

            // Items
            boolean alt = false;
            if (invoice.getItems() != null) {
                for (InvoiceItem item : invoice.getItems()) {
                    BaseColor bg = alt ? LIGHT_GRAY : BaseColor.WHITE;
                    addItemRow(itemsTable, item.getDescription(), bg,
                            String.valueOf(item.getQuantity()),
                            "₹" + item.getUnitPrice(),
                            "₹" + item.getTotalPrice());
                    alt = !alt;
                }
            }
            doc.add(itemsTable);
            doc.add(Chunk.NEWLINE);

            // ── Totals ──
            PdfPTable totals = new PdfPTable(2);
            totals.setWidthPercentage(50);
            totals.setHorizontalAlignment(Element.ALIGN_RIGHT);

            if (invoice.getDiscountAmount() != null && invoice.getDiscountAmount().doubleValue() > 0)
                addTotalRow(totals, "Discount:", "- ₹" + invoice.getDiscountAmount(), NORMAL_FONT);
            if (invoice.getTaxAmount() != null && invoice.getTaxAmount().doubleValue() > 0)
                addTotalRow(totals, "Tax:", "+ ₹" + invoice.getTaxAmount(), NORMAL_FONT);
            addTotalRow(totals, "TOTAL AMOUNT:", "₹" + invoice.getTotalAmount(), TOTAL_FONT);
            if ("PAID".equals(invoice.getStatus()))
                addTotalRow(totals, "Amount Paid:", "₹" + invoice.getAmountPaid(), NORMAL_FONT);

            doc.add(totals);
            doc.add(Chunk.NEWLINE);

            // ── Footer ──
            Paragraph footer = new Paragraph("Thank you for choosing MediChain HMS.\nFor queries: support@medichain.com", SMALL_FONT);
            footer.setAlignment(Element.ALIGN_CENTER);
            doc.add(footer);

            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("PDF generation failed: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate invoice PDF", e);
        }
    }

    private void addMetaRow(PdfPTable table, String label, String value) {
        PdfPCell l = new PdfPCell(new Phrase(label, HEADING_FONT));
        l.setBorder(Rectangle.BOTTOM);
        l.setBorderColor(new BaseColor(230, 230, 240));
        l.setPadding(6);
        table.addCell(l);
        PdfPCell v = new PdfPCell(new Phrase(value != null ? value : "-", NORMAL_FONT));
        v.setBorder(Rectangle.BOTTOM);
        v.setBorderColor(new BaseColor(230, 230, 240));
        v.setPadding(6);
        table.addCell(v);
    }

    private void addItemRow(PdfPTable table, String desc, BaseColor bg,
                             String qty, String unit, String total) {
        for (String val : new String[]{desc, qty, unit, total}) {
            PdfPCell c = new PdfPCell(new Phrase(val != null ? val : "-", NORMAL_FONT));
            c.setBackgroundColor(bg);
            c.setPadding(7);
            c.setBorder(Rectangle.BOTTOM);
            c.setBorderColor(new BaseColor(220, 225, 245));
            table.addCell(c);
        }
    }

    private void addTotalRow(PdfPTable table, String label, String value, Font font) {
        PdfPCell l = new PdfPCell(new Phrase(label, HEADING_FONT));
        l.setBorder(Rectangle.NO_BORDER);
        l.setPadding(5);
        table.addCell(l);
        PdfPCell v = new PdfPCell(new Phrase(value, font));
        v.setBorder(Rectangle.NO_BORDER);
        v.setPadding(5);
        v.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(v);
    }
}

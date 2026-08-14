# Boletas y facturas en RestoHub

## Datos almacenados

- Tipo de comprobante.
- Serie y número correlativo.
- Pedido relacionado.
- Cliente y documento.
- Razón social y dirección fiscal para factura.
- Operación gravada.
- IGV del 18 %.
- Total.
- Medio de pago.
- Usuario que realizó la emisión.
- Fecha de emisión.

## Reglas

- Cada pedido puede tener un solo comprobante.
- El comprobante solo se genera al registrar el pago.
- Un pedido cancelado no puede cobrarse.
- Un pedido con comprobante no puede eliminarse.
- Una factura exige RUC de 11 dígitos, razón social y dirección fiscal.
- Una boleta admite DNI de 8 dígitos o puede quedar sin documento en la demostración.

## PDF

`SalesDocumentPdf` genera un documento que puede compartirse mediante `FileProvider`.

## Alcance académico

El PDF es una representación interna. No constituye un comprobante electrónico validado o enviado a SUNAT.

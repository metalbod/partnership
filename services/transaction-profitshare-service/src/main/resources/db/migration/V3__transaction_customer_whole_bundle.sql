-- A transaction is now a customer's subscription to a partner's WHOLE bundle:
--  * the customer is registered directly (name, phone, email);
--  * no single offering or vendor is chosen, so those columns (and the Store-Front
--    enrolment reference) become optional. Legacy rows keep their old values.
ALTER TABLE transaction ADD COLUMN customer_name  VARCHAR(200) NOT NULL DEFAULT 'Unregistered (legacy)';
ALTER TABLE transaction ADD COLUMN customer_phone VARCHAR(50)  NOT NULL DEFAULT '';
ALTER TABLE transaction ADD COLUMN customer_email VARCHAR(254) NOT NULL DEFAULT '';
ALTER TABLE transaction ALTER COLUMN customer_name  DROP DEFAULT;
ALTER TABLE transaction ALTER COLUMN customer_phone DROP DEFAULT;
ALTER TABLE transaction ALTER COLUMN customer_email DROP DEFAULT;

ALTER TABLE transaction ALTER COLUMN consumer_enrolment_id DROP NOT NULL;
ALTER TABLE transaction ALTER COLUMN offering_id DROP NOT NULL;
ALTER TABLE transaction ALTER COLUMN vendor_id DROP NOT NULL;

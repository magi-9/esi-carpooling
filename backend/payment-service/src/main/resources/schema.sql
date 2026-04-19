CREATE TABLE IF NOT EXISTS payments (
  payment_id UUID PRIMARY KEY,
  booking_id VARCHAR(255) NOT NULL,
  payer_id VARCHAR(255) NOT NULL,
  payee_id VARCHAR(255) NOT NULL,
  amount DECIMAL(19,4) NOT NULL,
  currency VARCHAR(10) NOT NULL,
  status VARCHAR(30) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  completed_at TIMESTAMP,
  refund_id UUID,
  refund_amount DECIMAL(19,4),
  refund_currency VARCHAR(10),
  refund_reason TEXT,
  refund_processed_at TIMESTAMP
);

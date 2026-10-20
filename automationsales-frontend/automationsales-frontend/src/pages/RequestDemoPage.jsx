// =====================================================================
// Public "Request a demo" page (no login) — link to it from your website.
// Uses two public endpoints (see SecurityConfig):
//   GET  /api/products       to fill the product dropdown
//   POST /api/leads/public   creates the lead + assigns a salesperson + creates a task
// =====================================================================
import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { apiGet, apiPost } from '../api';
import { prepareForApi } from '../utils';
import { Field, FormError } from '../components/FormParts';

export default function RequestDemoPage() {
  const [products, setProducts] = useState([]);
  const [form, setForm] = useState({ leadName: '', companyName: '', email: '', phone: '', productId: '', message: '' });
  const [error, setError] = useState(null);
  const [sending, setSending] = useState(false);
  const [sent, setSent] = useState(false);

  useEffect(() => {
    apiGet('/products')
      .then((list) => setProducts(list.filter((p) => p.active)))
      .catch(() => setProducts([]));
  }, []);

  function handleChange(event) {
    setForm({ ...form, [event.target.name]: event.target.value });
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setSending(true);
    setError(null);
    try {
      await apiPost('/leads/public', prepareForApi(form, ['productId']));
      setSent(true);
    } catch (err) {
      setError(err);
      setSending(false);
    }
  }

  const fieldError = (name) => error?.fieldErrors?.[name];

  return (
    <div className="public-page">
      <header className="public-header">
        <div className="brand-logo"><span className="logo-mark">A</span> Sales Software</div>
        <Link to="/login">Staff sign in</Link>
      </header>

      <main className="public-main">
        <section className="public-intro">
          <h1>Get the right software licences, without the hassle.</h1>
          <p>Tell us what you need. A specialist will contact you within one working day with pricing and a short demo.</p>
          <ul className="check-list">
            <li>Volume pricing for teams of any size</li>
            <li>Renewals handled for you before anything expires</li>
            <li>One contact person for all your licences</li>
          </ul>
        </section>

        <div className="card public-form">
          {sent ? (
            <div className="success-box">
              <h2>Thank you! ✓</h2>
              <p>We've received your request. Someone from our team will be in touch shortly.</p>
            </div>
          ) : (
            <form className="form" onSubmit={handleSubmit}>
              <h2>Request a demo</h2>
              <FormError error={error} />

              <Field label="Your name *" error={fieldError('leadName')}>
                <input name="leadName" value={form.leadName} onChange={handleChange} required maxLength={100} autoComplete="name" />
              </Field>
              <Field label="Company *" error={fieldError('companyName')}>
                <input name="companyName" value={form.companyName} onChange={handleChange} required maxLength={150} autoComplete="organization" />
              </Field>
              <Field label="Work email *" error={fieldError('email')}>
                <input name="email" type="email" value={form.email} onChange={handleChange} required autoComplete="email" />
              </Field>
              <Field label="Phone *" error={fieldError('phone')}>
                <input name="phone" type="tel" value={form.phone} onChange={handleChange} required autoComplete="tel" placeholder="+971 50 000 0000" />
              </Field>
              <Field label="Product you're interested in *" error={fieldError('productId')}>
                <select name="productId" value={form.productId} onChange={handleChange} required>
                  <option value="">{products.length ? 'Choose a product…' : 'Loading products…'}</option>
                  {products.map((p) => <option key={p.productId} value={p.productId}>{p.productName}</option>)}
                </select>
              </Field>
              <Field label="Anything else we should know?" error={fieldError('message')}>
                <textarea name="message" rows={3} maxLength={2000} value={form.message} onChange={handleChange}
                  placeholder="Number of users, timeline, questions…" />
              </Field>

              <button type="submit" className="button button-primary button-full" disabled={sending}>
                {sending ? 'Sending…' : 'Request my demo'}
              </button>
              <p className="muted small">We only use your details to reply to this request.</p>
            </form>
          )}
        </div>
      </main>
    </div>
  );
}

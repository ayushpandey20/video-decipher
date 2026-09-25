document.addEventListener('DOMContentLoaded', () => {
    const decipherBtn = document.getElementById('decipherBtn');
    const videoUrlInput = document.getElementById('videoUrl');
    const resultsSection = document.getElementById('results');
    const resultText = document.getElementById('resultText');
    const loadingSpinner = document.getElementById('loadingSpinner');
    const downloadPdfBtn = document.getElementById('downloadPdfBtn');

    if (!decipherBtn) return;

    decipherBtn.addEventListener('click', async () => {
        const url = videoUrlInput.value.trim();

        if (!url) {
            alert('Please enter a valid YouTube video URL to decipher.');
            return;
        }

        if (loadingSpinner) loadingSpinner.classList.remove('hidden');
        if (resultsSection) resultsSection.classList.add('hidden');
        if (resultText) resultText.textContent = '';
        decipherBtn.disabled = true;
        decipherBtn.textContent = 'ANALYZING...';

        try {
            const response = await fetch('/api/ask', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ prompt: url })
            });

            const data = await response.json();

            if (resultText) {
                resultText.textContent = data.response || 'No data received.';
            }

            if (resultsSection) {
                resultsSection.classList.remove('hidden');
                resultsSection.scrollIntoView({ behavior: 'smooth' });
            }

        } catch (error) {
            console.error('Connection failure:', error);
            if (resultText) {
                resultText.textContent = `[EXTREME ERROR]: Communication breakdown.\nDetails: ${error.message}`;
            }
            if (resultsSection) {
                resultsSection.classList.remove('hidden');
                resultsSection.scrollIntoView({ behavior: 'smooth' });
            }
        } finally {
            if (loadingSpinner) loadingSpinner.classList.add('hidden');
            decipherBtn.disabled = false;
            decipherBtn.textContent = 'ANALYZE VIDEO';
        }
    });

    if (downloadPdfBtn) {
        downloadPdfBtn.addEventListener('click', async () => {
            const summaryText = resultText.textContent;
            const videoUrl = videoUrlInput.value.trim() || 'COSMIC SOURCE';

            if (!summaryText || resultsSection.classList.contains('hidden')) {
                alert('No video analysis available for export yet.');
                return;
            }

            // 1. Create the container element
            const pdfElement = document.createElement('div');
            pdfElement.id = 'tempPdfContainer';
            
            // 2. Set strict dimensions & styling so html2canvas renders full height
            pdfElement.style.width = '700px'; 
            pdfElement.style.padding = '40px';
            pdfElement.style.fontFamily = "'Outfit', sans-serif";
            pdfElement.style.color = '#111';
            pdfElement.style.lineHeight = '1.7';
            pdfElement.style.background = '#ffffff';
            
            // 3. Position off-screen so it's in the DOM but invisible to the user
            pdfElement.style.position = 'absolute';
            pdfElement.style.left = '-9999px';
            pdfElement.style.top = '0';

            // 4. Escape raw HTML characters in summaryText to prevent template breakage
            const safeSummaryText = summaryText
                .replace(/&/g, '&amp;')
                .replace(/</g, '&lt;')
                .replace(/>/g, '&gt;');

            pdfElement.innerHTML = `
                <div style="border-bottom: 2px solid #00f0ff; padding-bottom: 15px; margin-bottom: 25px;">
                    <h1 style="color: #0a0c16; font-size: 20px; margin: 0; letter-spacing: 1px;">EXTREME DECIPHER REPORT</h1>
                    <p style="color: #666; font-size: 11px; margin: 3px 0 0 0;">Cosmic Video Intelligence</p>
                </div>
                
                <div style="margin-bottom: 20px; background: #f4f6ff; padding: 12px; border-radius: 4px; border-left: 3px solid #e024ff;">
                    <strong style="font-size: 11px; color: #555;">SOURCE LINK:</strong>
                    <p style="font-size: 12px; word-break: break-all; margin: 3px 0 0 0; color: #0055aa;">${videoUrl}</p>
                </div>

                <h3 style="color: #0a0c16; font-size: 15px; font-weight: 700; margin-bottom: 10px; border-bottom: 1px solid #ddd; padding-bottom: 5px;">ANALYTICAL BREAKDOWN</h3>
                <div style="white-space: pre-wrap; font-size: 12px; color: #222;">${safeSummaryText}</div>

                <div style="margin-top: 40px; border-top: 1px solid #eee; padding-top: 15px; text-align: center; font-size: 10px; color: #888;">
                    Generated by Extreme Astro-Analytics Engine
                </div>
            `;

            // 5. Append to DOM so html2canvas can measure full scrollHeight
            document.body.appendChild(pdfElement);

            const opt = {
                margin:       10,
                filename:     'Extreme_Decipher_Summary.pdf',
                image:        { type: 'jpeg', quality: 0.98 },
                html2canvas:  { scale: 2, useCORS: true, scrollY: 0 },
                jsPDF:        { unit: 'mm', format: 'a4', orientation: 'portrait' }
            };

            try {
                // 6. Generate and save PDF
                await html2pdf().set(opt).from(pdfElement).save();
            } catch (err) {
                console.error('PDF generation error:', err);
            } finally {
                // 7. Clean up by removing the temporary element from DOM
                pdfElement.remove();
            }
        });
    }
});

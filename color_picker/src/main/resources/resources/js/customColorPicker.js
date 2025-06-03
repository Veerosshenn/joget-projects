(function(window, document, Math, undefined) {
    if (window.Coloris) {
        const colorisInstance = window.Coloris;
        const settings = colorisInstance.settings || {};

        var favoriteColors = JSON.parse(localStorage.getItem('favoriteColors')) || [];

        /**
         * Check if EyeDropper API is supported
         * @returns {boolean} True if EyeDropper is supported
         */
        function hasEyeDropperSupport() {
            return 'EyeDropper' in window;
        }

        /**
         * Function to save the selected color to localStorage
         * @param {string} color - The color to save
         */
        function saveColor(color) {
            if (!favoriteColors.includes(color)) {
                favoriteColors.push(color);
                localStorage.setItem('favoriteColors', JSON.stringify(favoriteColors));
                renderSwatches();
            } else {
                alert("This color is already in your favorites.");
            }
        }


        // Function to render swatches, including the favorite colors
        function renderSwatches() {
            // Retry rendering with multiple attempts if the DOM isn't ready
            let attempts = 0;
            const maxAttempts = 5;
            const attemptRender = () => {
                const picker = document.querySelector('.clr-picker');
                const swatchesContainer = document.getElementById("clr-swatches");
                if (!picker || !swatchesContainer || attempts >= maxAttempts) {
                    if (attempts < maxAttempts) {
                        attempts++;
                        setTimeout(attemptRender, 200);
                    }
                    return;
                }

                let innerWrapper = swatchesContainer.querySelector("div");
                if (!innerWrapper) {
                    innerWrapper = document.createElement("div");
                    swatchesContainer.appendChild(innerWrapper);
                }

                const favoriteSwatches = innerWrapper.querySelectorAll('[data-favorite-color]');
                favoriteSwatches.forEach(swatch => swatch.remove());

                const favoriteColors = JSON.parse(localStorage.getItem('favoriteColors')) || [];

                favoriteColors.forEach((color, i) => {
                    // Create a new swatch button for the favorite color
                    const button = document.createElement("button");
                    button.type = "button";
                    button.id = `clr-swatch-${innerWrapper.children.length}`; // Ensure unique ID
                    button.setAttribute("aria-labelledby", `clr-swatch-label clr-swatch-${innerWrapper.children.length}`);
                    button.setAttribute("data-favorite-color", color.toLowerCase()); // Mark as favorite color
                    button.style.color = color;
                    button.textContent = color;

                    button.addEventListener("click", () => {
                        const input = document.querySelector(".clr-field input");
                        const preview = document.querySelector(".clr-preview");
                        if (input) input.value = color;
                        if (preview) preview.style.backgroundColor = color;
                        input.dispatchEvent(new Event('input')); // Trigger Coloris update
                    });

                    innerWrapper.appendChild(button);
                });
            };

            attemptRender();
        }


        // Add a single eyedropper button to the left of the vertically stacked sliders
        function addEyeDropperButton() {
            const hueSlider = document.querySelector(".clr-hue");
            const alphaSlider = document.querySelector(".clr-alpha");
            if (!hueSlider || !alphaSlider) return;

            // Check if already wrapped to avoid duplicates
            if (hueSlider.parentElement.classList.contains("clr-sliders-inner")) return;

            // Create wrapper for eyedropper and sliders
            const wrapper = document.createElement("div");
            wrapper.className = "clr-sliders-wrapper";

            // Create inner container for sliders
            const innerContainer = document.createElement("div");
            innerContainer.className = "clr-sliders-inner";

            // Create eyedropper button
            const eyeDropperButton = document.createElement("button");
            eyeDropperButton.type = "button";
            eyeDropperButton.className = "clr-eyedropper";
            eyeDropperButton.innerHTML = `
                <svg fill="#000000" width="50px" height="50px" viewBox="0 0 32 32" version="1.1"
                     xmlns="http://www.w3.org/2000/svg">
                    <title>Eyedropper</title>
                    <path
                        d="M24.552 13.812l-6.364-6.364c-0.39-0.391-0.39-1.023 0-1.414 0.391-0.391 1.024-0.391 1.415 0l0.707 0.707 3.183-3.182c1.366-1.367 3.582-1.367 4.949 0 1.367 1.366 1.367 3.583 0 4.949l-3.182 3.183 0.707 0.707c0.391 0.391 0.391 1.023 0 1.414s-1.025 0.391-1.415 0zM9.602 27.348c-0.619 0.619-2.165-0.662-2.828 0l-1.68 1.68c-0.585 0.586-1.535 0.586-2.121 0s-0.586-1.535 0-2.121l1.68-1.68c0.663-0.662-0.619-2.209 0-2.828 0.618-0.62 13.523-13.524 13.523-13.524l4.949 4.95c0 0-12.904 12.904-13.523 13.523zM21.711 13.825l-3.535-3.536c0 0-11.491 11.491-12.109 12.11s0.619 2.209 0 2.828l-2.387 2.386c-0.195 0.195-0.195 0.512 0 0.707s0.512 0.195 0.707 0l2.387-2.387c0.619-0.619 2.209 0.619 2.828 0s12.109-12.108 12.109-12.108z"></path>
                </svg>`;
            eyeDropperButton.title = "Pick a color from the screen";

            // Insert wrapper and append elements
            hueSlider.parentElement.insertBefore(wrapper, hueSlider);
            wrapper.appendChild(eyeDropperButton); // EyeDropper on the left
            wrapper.appendChild(innerContainer); // Sliders on the right

            // Move sliders into inner container
            innerContainer.appendChild(hueSlider);
            innerContainer.appendChild(alphaSlider);

            // Adjust inner container width if EyeDropper is supported
            if (hasEyeDropperSupport()) {
                innerContainer.classList.add("clr-sliders-adjusted");
                hueSlider.classList.add("clr-slider-adjusted");
                alphaSlider.classList.add("clr-slider-adjusted");
            } else {
                eyeDropperButton.disabled = true;
                eyeDropperButton.title = "EyeDropper not supported in this browser";
                innerContainer.classList.add("clr-sliders-adjusted");
                hueSlider.classList.add("clr-slider-adjusted");
                alphaSlider.classList.add("clr-slider-adjusted");
            }

            eyeDropperButton.addEventListener("click", async () => {
                if (!hasEyeDropperSupport()) return;

                try {
                    const eyeDropper = new EyeDropper();
                    const result = await eyeDropper.open();
                    const selectedColor = result.sRGBHex; // e.g., "#FF0000"

                    // Update the Coloris input and preview
                    const input = document.querySelector(".clr-field input");
                    const preview = document.querySelector(".clr-preview");
                    if (input) {
                        input.value = selectedColor;

                        // Dispatch both input and change events to ensure Coloris updates
                        input.dispatchEvent(new Event('input', { bubbles: true }));
                        input.dispatchEvent(new Event('change', { bubbles: true }));
                    }
                    if (preview) {
                        preview.style.backgroundColor = selectedColor;
                    }

                    // Force Coloris to update by simulating a focus or click if needed
                    setTimeout(() => {
                        if (input) {
                            input.focus();
                            input.click();
                        }
                    }, 0);
                } catch (error) {
                    console.error("EyeDropper error:", error.message);
                }
            });
        }


         // Initialize functionality after page load
        window.addEventListener('load', function () {
            // Add save button functionality
            var saveButton = document.getElementById('clr-save');
            if (saveButton) {
                saveButton.addEventListener('click', function () {
                    const selectedColor = document.getElementById('clr-color-value').value;
                    saveColor(selectedColor);
                });
            }

            addEyeDropperButton();

            renderSwatches();

            // Use MutationObserver to detect when the Coloris picker's swatches container is added
            const observer = new MutationObserver((mutations) => {
                mutations.forEach((mutation) => {
                    if (mutation.addedNodes.length) {
                        const swatchesContainer = document.getElementById('clr-swatches');
                        if (swatchesContainer && Array.from(mutation.addedNodes).some(node => node.contains(swatchesContainer))) {
                            // Add a small delay to ensure the DOM is fully initialized
                            setTimeout(() => {
                                renderSwatches();
                            }, 300);
                        }
                    }
                });
            });

            // Observe the body for changes to detect swatches container addition
            observer.observe(document.body, {
                childList: true,
                subtree: true
            });

            // Fallback: Listen for clicks on the Coloris input to trigger rendering
            const colorisInput = document.querySelector('.clr-field input');
            if (colorisInput) {
                colorisInput.addEventListener('click', () => {
                    setTimeout(() => {
                        renderSwatches();
                    }, 300);
                });
            }
        });
    }
})(window, document, Math);
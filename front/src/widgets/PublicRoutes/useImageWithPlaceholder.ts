import { useEffect, useState } from 'react';

export function useImageWithPlaceholder(imageUrl: string | undefined, placeholderUrl: string): string {
  const [resolvedSrc, setResolvedSrc] = useState(placeholderUrl);

  useEffect(() => {
    setResolvedSrc(placeholderUrl);

    if (!imageUrl) {
      return;
    }

    let active = true;
    const image = new Image();

    image.onload = () => {
      if (active) {
        setResolvedSrc(imageUrl);
      }
    };

    image.onerror = () => {
      if (active) {
        setResolvedSrc(placeholderUrl);
      }
    };

    image.src = imageUrl;

    return () => {
      active = false;
    };
  }, [imageUrl, placeholderUrl]);

  return resolvedSrc;
}

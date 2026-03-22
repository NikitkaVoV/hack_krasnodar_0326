import { collectRoutePdfSource } from './collectRoutePdfSource';
import { generateRoutePdfDocument } from './generateRoutePdfDocument';
import { mapRoutePdfSourceToModel, toPdfFileName } from './routePdfMapper';

export async function exportRouteToPdf(routeId: string): Promise<void> {
  const source = await collectRoutePdfSource(routeId);
  const model = mapRoutePdfSourceToModel(source);
  const blob = await generateRoutePdfDocument(model);

  const fileName = toPdfFileName(model);
  const objectUrl = URL.createObjectURL(blob);

  const link = document.createElement('a');
  link.href = objectUrl;
  link.download = fileName;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);

  URL.revokeObjectURL(objectUrl);
}
